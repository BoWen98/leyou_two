package com.leyou.page.listener;

import com.leyou.page.service.PageService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ItemListener {

    @Autowired
    private PageService pageService;

    //消费者方法,接收新增和修改商品的消息
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "ly.page.insert.queue", durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"item.insert", "item.update"}))
    public void listenInsert(Long id) {
        if (null != id) {
            //新增或修改
            pageService.createItemHtml(id);
        }
    }

    //消费者方法,接收删除商品的消息
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "ly.page.delete.queue", durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = "item.delete"))
    public void listenDelete(Long id) {
        if (null != id) {
            //删除
            pageService.deleteItemHtml(id);
        }
    }
}
