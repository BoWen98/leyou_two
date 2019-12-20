package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.order.config.PayConfig;
import com.leyou.order.eunms.OrderStatusEnum;
import com.leyou.order.eunms.PayState;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PayHelper {

    @Autowired
    private WXPay wxPay;

    @Autowired
    private PayConfig payConfig;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusMapper statusMapper;

    private static final String TRADE_TYPE = "NATIVE";

    public PayState queryPayState(Long orderId) {
        try {
            //组织请求参数
            Map<String, String> data = new HashMap<>();
            //订单号
            data.put("out_trade_no", orderId.toString());
            //查询状态
            Map<String, String> result = wxPay.orderQuery(data);

            //校验通信状态
            isConnectSuccess(result);
            //校验业务状态
            isBusinessSuccess(result);
            //校验签名
            isSignatureValid(result);

            //校验金额
            String totalFeeStr = result.get("total_fee");
            String tradeNo = result.get("out_trade_no");
            if (StringUtils.isEmpty(totalFeeStr) || StringUtils.isEmpty(tradeNo)) {
                throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
            }
            //获取结果中的金额
            Long totalFee = Long.valueOf(totalFeeStr);
            //获取订单金额
            Order order = orderMapper.selectByPrimaryKey(orderId);
            if (totalFee !=/*order.getActualPay()*/ 1L) {
                //金额不符
                throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
            }
            String state = result.get("trade_state");
            if ("SUCCESS".startsWith(state)) {
                //支付成功
                //修改订单状态
                OrderStatus status = new OrderStatus();
                status.setStatus(OrderStatusEnum.PAY_UP.value());
                status.setOrderId(orderId);
                status.setPaymentTime(new Date());
                int count = statusMapper.updateByPrimaryKeySelective(status);
                if (count != 1) {
                    throw new LyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
                }
                //返回成功
                return PayState.SUCCESS;
            }
            //未支付
            if ("NOTPAY".equals(state) || "USERPAYING".equals(state)) {
                return PayState.NOT_PAY;
            }
            //支付失败
            return PayState.FAIL;
        } catch (Exception e) {
            return PayState.NOT_PAY;
        }
    }

    /**
     * 获取支付的url链接
     * @param orderId
     * @param desc
     * @param totalPay
     * @return
     */
    public String getPayUrl(String orderId, String desc, String totalPay) {
        try {
            //准备请求参数
            Map<String, String> data = new HashMap<>();
            data.put("body", desc);
            data.put("out_trade_no", orderId);
            data.put("total_fee", totalPay);
            data.put("spbill_create_ip", "123.12.12.123");
            data.put("notify_url", payConfig.getNotifyUrl());
            data.put("trade_type", TRADE_TYPE);
            Map<String, String> resp = wxPay.unifiedOrder(data);
            //校验通信状态
            isConnectSuccess(resp);
            //校验业务状态
            isBusinessSuccess(resp);
            // 校验签名
            isSignatureValid(resp);
            return resp.get("code_url");
        } catch (Exception e) {
            log.error("【微信支付】微信下单失败。", e);
            throw new LyException(ExceptionEnum.WX_ORDER_ERROR);
        }
    }

    public void isBusinessSuccess(Map<String, String> resp) {
        //校验业务标识
        if ("FAIL".equals(resp.get("result_code"))) {
            log.error("【微信支付】微信下单失败，原因：{}", resp.get("err_code_des"));
            throw new LyException(ExceptionEnum.WX_ORDER_ERROR);
        }
    }

    public void isConnectSuccess(Map<String, String> resp) {
        //校验通信标识
        if ("FAIL".equals(resp.get("return_code"))) {
            log.error("【微信支付】微信支付通信失败，原因：{}", resp.get("return_msg"));
            throw new LyException(ExceptionEnum.WX_CONNECTION_ERROR);
        }
    }

    public void isSignatureValid(Map<String, String> resp) {
        try {
            boolean boo1 = WXPayUtil.isSignatureValid(resp, payConfig.getKey(), WXPayConstants.SignType.HMACSHA256);
            boolean boo2 = WXPayUtil.isSignatureValid(resp, payConfig.getKey());
            if (!boo1 && !boo2) {
                //签名有误
                log.error("【微信支付】微信签名无效。");
                throw new LyException(ExceptionEnum.WX_SIGNATURE_INVALID);
            }
        } catch (Exception e) {
            log.error("【微信支付】微信签名无效。",e);
            throw new LyException(ExceptionEnum.WX_SIGNATURE_INVALID);
        }
    }
}
