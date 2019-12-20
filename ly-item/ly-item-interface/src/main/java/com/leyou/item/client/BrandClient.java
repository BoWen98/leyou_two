package com.leyou.item.client;

import com.leyou.pojo.Brand;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "item-service",path = "brand")
public interface BrandClient {

    /**
     * 根据品牌id查询品牌的接口
     * @param id
     * @return
     */
    @GetMapping("{id}")
    Brand queryBrandById(@PathVariable("id") Long id);

    @GetMapping("ids")
    List<Brand> queryBrandByIds(@RequestParam("ids") List<Long> ids);
}
