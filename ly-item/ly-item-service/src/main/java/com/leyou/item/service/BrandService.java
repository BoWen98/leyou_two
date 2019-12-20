package com.leyou.item.service;

import com.leyou.common.vo.PageResult;
import com.leyou.pojo.Brand;

import java.util.List;

public interface BrandService {
    PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String key, String sortBy, Boolean desc);

    void saveBrand(Brand brand, List<Long> cids);

    void deleteByBrandId(Long bid);

    void updateBrand(Brand brand, List<Long> cids);

    Brand queryBrandById(Long brandId);

    List<Brand> queryBrandByCid(Long cid);

    List<Brand> queryBrandByIds(List<Long> ids);
}
