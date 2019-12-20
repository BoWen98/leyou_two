package com.leyou.cart.listener;

import com.leyou.cart.service.CartService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CartListener {

    @Autowired
    private CartService cartService;

    //消费者方法,接收新增和修改商品的消息
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "ly.cart.delete.queue", durable = "true"),
            exchange = @Exchange(name = "ly.cart.exchange", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = "cart.delete"))
    public void listenDelete(Map<Long, List<Long>> map) {
        if (null != map) {
            //新增或修改
            cartService.deleteCarts(map);
        }
    }
}
