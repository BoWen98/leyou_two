package com.leyou.order.service;

import com.leyou.common.dto.OrderDTO;
import com.leyou.order.eunms.PayState;
import com.leyou.order.pojo.Order;

import java.util.List;
import java.util.Map;

public interface OrderService {
    Long createOrder(OrderDTO orderDTO);

    Order queryOrderById(Long orderId);

    String queryPayUrl(Long orderId);

    PayState queryPayState(Long orderId);

    void handleNotify(Map<String, String> data);

    List<Order> queryAllOrder(String status, String key, Integer page, Integer rows);

    void clearTimeoutOrder();

}
