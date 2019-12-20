package com.leyou.order.service.impl;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.dto.OrderDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.client.GoodsClient;
import com.leyou.order.eunms.OrderStatusEnum;
import com.leyou.order.eunms.PayState;
import com.leyou.order.interceptors.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.service.OrderService;
import com.leyou.order.utils.PayHelper;
import com.leyou.pojo.AddressDTO;
import com.leyou.pojo.Sku;
import com.leyou.user.AddressClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper detailMapper;

    @Autowired
    private OrderStatusMapper statusMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private AddressClient addressClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private PayHelper payHelper;

    @Override
    @Transactional
    public Long createOrder(OrderDTO orderDTO) {
        //1.准备order数据
        Order order = new Order();
        //1.1订单的id
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        //1.2买家信息
        UserInfo loginUser = UserInterceptor.getLoginUser();
        order.setUserId(loginUser.getId());
        order.setBuyerNick(loginUser.getUsername());
        order.setBuyerRate(false);

        //1.3根据id查询收件人信息
        loadAddressInOrder(orderDTO.getAddressId(), order);

        //1.4订单金额,并且封装OrderDetail集合
        List<OrderDetail> orderDetails = getTotalPayAndOrderDetail(orderDTO, order, orderId);

        //2.新增订单
        order.setCreateTime(new Date());
        int count = orderMapper.insertSelective(order);
        if (count != 1) {
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        //3.新增OrderDetail
        count = detailMapper.insertList(orderDetails);
        if (count != orderDetails.size()) {
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        //4.准备OrderStatus
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.INIT.value());
        orderStatus.setCreateTime(order.getCreateTime());
        count = statusMapper.insertSelective(orderStatus);
        if (count != 1) {
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        //5.减库存
        goodsClient.decreaseStock(orderDTO.getCarts());

        //6.删除购物车商品
        List<CartDTO> carts = orderDTO.getCarts();
        //获取sku的id集合
        List<Long> idList = carts.stream().map(CartDTO::getSkuId).collect(Collectors.toList());
        Map<Long, List<Long>> map = new HashMap<>();
        map.put(loginUser.getId(), idList);
        amqpTemplate.convertAndSend("ly.cart.exchange", "cart.delete", map);

        //7.返回OrderId
        return orderId;
    }

    /**
     * 根据订单id查询订单
     *
     * @param orderId
     * @return
     */
    @Override
    public Order queryOrderById(Long orderId) {
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (null == order) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        //查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orderId);
        List<OrderDetail> orderDetails = detailMapper.select(detail);
        if (CollectionUtils.isEmpty(orderDetails)) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        //订单详情存入order中
        order.setOrderDetails(orderDetails);
        //查询订单状态
        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(orderId);
        order.setOrderStatus(orderStatus);
        if (null == orderStatus) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        return order;
    }

    /**
     * 根据订单id,生成支付链接
     *
     * @param orderId
     * @return
     */
    @Override
    public String queryPayUrl(Long orderId) {
        //查询order
        Order order = queryOrderById(orderId);
        //判断订单状态
        if (order.getOrderStatus().getStatus() != OrderStatusEnum.INIT.value()) {
            //说明已经支付,抛出异常
            throw new LyException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        //商品描述
        String desc = order.getOrderDetails().stream().map(OrderDetail::getTitle).collect(Collectors.joining(","));
        //支付的实付金额
        Long totalPay = /*order.getActualPay()*/1L;
        //生成url并返回
        return payHelper.getPayUrl(orderId.toString(), desc, totalPay.toString());
    }

    /**
     * 查询支付状态
     *
     * @param orderId
     * @return
     */
    @Override
    public PayState queryPayState(Long orderId) {
        //先查询订单状态
        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(orderId);
        if (orderStatus.getStatus() != OrderStatusEnum.INIT.value()) {
            //说明已经支付
            return PayState.SUCCESS;
        }
        //未支付,再去查询微信系统
        return payHelper.queryPayState(orderId);
    }

    @Override
    public void handleNotify(Map<String, String> data) {

        //校验通信
        payHelper.isConnectSuccess(data);
        //校验签名
        payHelper.isSignatureValid(data);
        //校验是否处理过该消息
        String tradeNo = data.get("out_trade_no");
        String totalFee = data.get("total_fee");
        if (StringUtils.isBlank(totalFee) || StringUtils.isBlank(tradeNo)) {
            return;
        }
        Long orderId = Long.valueOf(tradeNo);
        Order order = queryOrderById(orderId);
        if (order.getOrderStatus().getStatus() != 1) {
            //说明已经支付
            return;
        }
        //校验金额
        Long totalPay = Long.valueOf(totalFee);
        if (/*order.getActualPay()*/ 1L != totalPay) {
            return;
        }
        //业务标识
        if ("FAIL".equals(data.get("result_code"))) {
            //支付失败
            return;
        }
        //如果成功,修改订单的状态为已支付
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.PAY_UP.value());
        orderStatus.setPaymentTime(new Date());
        statusMapper.updateByPrimaryKeySelective(orderStatus);
    }

    /**
     * 清除超过30分钟的订单,并撤回sku存储数量
     */
    @Transactional
    @Override
    public void clearTimeoutOrder() {
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setStatus(OrderStatusEnum.INIT.value());
        List<OrderStatus> orderStatuses = statusMapper.select(orderStatus);
        Date now = new Date();
        long nowTime = now.getTime();
        for (OrderStatus status : orderStatuses) {
            Date create = status.getCreateTime();
            long createTime = create.getTime();
            long minute = (nowTime - createTime) / 60000;
            if (minute > 30) {
                Long orderId = status.getOrderId();
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrderId(orderId);
                List<OrderDetail> orderDetails = detailMapper.select(orderDetail);
                Map<Long, Integer> map = new HashMap<>();
                for (OrderDetail detail : orderDetails) {
                    Long skuId = detail.getSkuId();
                    Integer num = detail.getNum();
                    map.put(skuId, num);
                }
                goodsClient.creaseStock(map);
                status.setStatus(OrderStatusEnum.repeal.value());
                int count = statusMapper.updateByPrimaryKeySelective(status);
                if (1 != count) {
                    throw new LyException(ExceptionEnum.WX_ORDER_ERROR);
                }
            }
        }
    }

    /**
     * 查询当前用户的所有订单
     *
     * @param status
     * @param page
     * @param rows
     * @param key
     * @return
     */
    @Override
    public List<Order> queryAllOrder(String status, String key, Integer page, Integer rows) {
        //获取登录的用户信息
        UserInfo loginUser = UserInterceptor.getLoginUser();
        Long userId = loginUser.getId();
        Integer startIndex = (page - 1) * rows;
        //查询
        List<Order> orderList = null;
        if (StringUtils.isNotBlank(status) && StringUtils.isNotBlank(key)) {
            orderList = orderMapper.selectOrderByUserIdAndStatusAndKey(userId, status, key, startIndex, rows);
        } else if (StringUtils.isNotBlank(status) && StringUtils.isBlank(key)) {
            orderList = orderMapper.selectOrderByUserIdAndStatus(userId, status, startIndex, rows);
        } else if (StringUtils.isBlank(status) && StringUtils.isNotBlank(key)) {
            orderList = orderMapper.selectOrderByUserIdAndKey(userId, key, startIndex, rows);
        } else {
            orderList = orderMapper.selectOrderByUserId(userId, startIndex, rows);
        }

        for (Order order : orderList) {
            Long orderId = order.getOrderId();
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(orderId);
            List<OrderDetail> orderDetails = detailMapper.select(detail);
            OrderStatus orderStatus = statusMapper.selectByPrimaryKey(orderId);
            order.setOrderStatus(orderStatus);
            order.setOrderDetails(orderDetails);
        }
        return orderList;
    }

    private void loadAddressInOrder(Long addressId, Order order) {
        AddressDTO address = addressClient.queryAddressById(addressId);
        order.setReceiverState(address.getState());
        order.setReceiverCity(address.getCity());
        order.setReceiverDistrict(address.getDistrict());
        order.setReceiverAddress(address.getAddress());
        order.setReceiver(address.getName());
        order.setReceiverMobile(address.getPhone());
    }

    private List<OrderDetail> getTotalPayAndOrderDetail(OrderDTO orderDTO, Order order, long orderId) {
        List<CartDTO> carts = orderDTO.getCarts();
        //获取sku的id集合
        List<Long> idList = carts.stream().map(CartDTO::getSkuId).collect(Collectors.toList());
        //把carts转成map,key是skuId,值是num
        Map<Long, Integer> numMap = carts.stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        //1.4.1查询sku
        List<Sku> skuList = goodsClient.querySkuByIds(idList);
        //1.4.2计算总金额
        long total = 0;
        //准备OrderDetail的集合
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (Sku sku : skuList) {
            //计算总金额
            int num = numMap.get(sku.getId());
            total += sku.getPrice() * num;
            //创建OrderDetail
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(orderId);
            detail.setTitle(sku.getTitle());
            detail.setPrice(sku.getPrice());
            detail.setOwnSpec(sku.getOwnSpec());
            detail.setSkuId(sku.getId());
            detail.setNum(num);
            detail.setImage(StringUtils.substringBefore(sku.getImages(), ","));
            orderDetails.add(detail);
        }
        //1.4.3填数据
        order.setTotalPay(total);
        order.setPaymentType(orderDTO.getPaymentType());
        order.setActualPay(total + order.getPostFee());
        return orderDetails;
    }
}
