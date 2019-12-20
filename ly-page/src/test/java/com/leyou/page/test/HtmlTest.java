package com.leyou.page.test;

import com.leyou.item.client.GoodsClient;
import com.leyou.pojo.Spu;
import com.leyou.page.service.PageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HtmlTest {

    @Autowired
    private PageService pageService;

    @Autowired
    private GoodsClient goodsClient;

    @Test
    public void testCreateHtml() {
        List<Spu> spuList = goodsClient.queryAllSpuId();
        for (Spu spu : spuList) {
            Long id = spu.getId();
            pageService.createItemHtml(id);
        }
    }
}
