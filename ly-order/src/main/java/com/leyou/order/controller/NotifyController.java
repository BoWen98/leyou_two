package com.leyou.order.controller;

import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class NotifyController {

    @Autowired
    private OrderService orderService;

    /**
     * 接收微信的异步回调通知
     * @param data
     * @return
     */
    @PostMapping(value = "/wxpay/notify", produces = "application/yml")
    public ResponseEntity<Map<String, String>> handleNotify(@RequestBody Map<String, String> data) {
        orderService.handleNotify(data);
        Map<String, String> result = new HashMap<>();
        result.put("return_code", "SUCCESS");
        result.put("return_msg", "OK");
        log.info("【微信支付】收到支付结果，{}", data);
        return ResponseEntity.ok(result);
    }
}
