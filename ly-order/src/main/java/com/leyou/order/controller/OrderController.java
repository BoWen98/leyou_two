package com.leyou.order.controller;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.common.dto.OrderDTO;
import com.leyou.order.interceptors.UserInterceptor;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import com.leyou.pojo.AddressDTO;
import com.leyou.user.AddressClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {

    @Autowired
    private AddressClient addressClient;

    @Autowired
    private OrderService orderService;

    /**
     * 查询收货地址
     *
     * @param token
     * @return
     */
    @GetMapping("/address/list")
    public ResponseEntity<List<AddressDTO>> queryAddress(@CookieValue("LY_TOKEN") String token) {
        UserInfo loginUser = UserInterceptor.getLoginUser();
        if (loginUser != null) {
            return ResponseEntity.ok(addressClient.queryAddress(loginUser.getId()));
        }
        return null;
    }

    /**
     * 新增订单
     *
     * @param orderDTO
     * @return
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDTO orderDTO) {
        Long orderId = orderService.createOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }

    /**
     * 根据订单id查询订单
     *
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(orderService.queryOrderById(id));
    }

    /**
     * 根据订单编号,生成支付链接并返回
     *
     * @param orderId
     * @return
     */
    @GetMapping("/url/{orderId}")
    public ResponseEntity<String> queryPayUrl(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(orderService.queryPayUrl(orderId));
    }

    /**
     * 查询支付状态
     *
     * @param orderId
     * @return
     */
    @GetMapping("/state/{orderId}")
    public ResponseEntity<Integer> queryPayState(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(orderService.queryPayState(orderId).getValue());
    }


    /**
     * 查询当前用户的所有订单
     *
     * @param status
     * @param page
     * @param rows
     * @param token
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<List<Order>> queryAllOrder(@RequestParam(value = "status", required = false) String status,
                                                     @RequestParam(value = "key", required = false) String key,
                                                     @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                     @RequestParam(value = "rows", defaultValue = "5") Integer rows,
                                                     @CookieValue("LY_TOKEN") String token) {
        return ResponseEntity.ok(orderService.queryAllOrder(status, key, page, rows));

    }

    /**
     * 定时清除超时订单,返回sku库存数量
     *
     * @param
     * @return
     */
    @GetMapping("repeal")
    public ResponseEntity<Void> clearTimeoutOrder() {
        orderService.clearTimeoutOrder();
        return ResponseEntity.ok().build();
    }
}
