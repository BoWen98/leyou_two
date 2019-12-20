package com.leyou.page.service;

import com.leyou.item.client.BrandClient;
import com.leyou.item.client.CategoryClient;
import com.leyou.item.client.GoodsClient;
import com.leyou.item.client.SpecificationClient;
import com.leyou.pojo.Brand;
import com.leyou.pojo.Category;
import com.leyou.pojo.SpecGroup;
import com.leyou.pojo.Spu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PageService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public Map<String, Object> loadItemPage(Long spuId) {
        //查询spu
        Spu spu = goodsClient.querySpuBySpuId(spuId);
        //查询分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        //查询规格
        List<SpecGroup> specs = specificationClient.querySpecsByCid(spu.getCid3());
        //封装数据
        Map<String, Object> data = new HashMap<>();
        data.put("categories", categories);
        data.put("brand", brand);
        data.put("title", spu.getTitle());
        data.put("subTitle", spu.getSubTitle());
        data.put("detail", spu.getSpuDetail());
        data.put("skus", spu.getSkus());
        data.put("specs", specs);
        return data;
    }

    //创建静态html文件
    public void createItemHtml(Long spuId) {
        //创建上下文
        Context context = new Context();
        //给上下文添加数据
        context.setVariables(loadItemPage(spuId));

        //准备目标文件
        File dir = new File("D:\\nginx-1.14.0\\html\\item");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File filePath = new File(dir, spuId + ".html");
        //创建输出流,关联一个文件
        try (PrintWriter writer = new PrintWriter(filePath)) {
            //利用模板引擎,输出页面内容到文件
            templateEngine.process("item", context, writer);
        } catch (IOException e) {
            log.error("【静态页服务】创建商品静态页失败，商品id：{}", spuId, e);
        }
    }

    //删除html静态文件
    public void deleteItemHtml(Long id){
        File file = new File("D:\\nginx-1.14.0\\html\\item\\" + id + ".html");
        if (file.exists()) {
            file.delete();
        }
    }

}
