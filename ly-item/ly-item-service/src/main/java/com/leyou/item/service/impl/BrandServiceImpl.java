package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.service.BrandService;
import com.leyou.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    /**
     * 分页查询品牌信息
     * @param page
     * @param rows
     * @param key
     * @param sortBy
     * @param desc
     * @return
     */
    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String key, String sortBy, Boolean desc) {
        //分页
        PageHelper.startPage(page, rows);
        //过滤条件
        Example example = new Example(Brand.class);
        if (StringUtils.isNoneBlank(key)) {
            example.createCriteria().orLike("name", "%" + key + "%").orEqualTo("letter", key.toUpperCase());
        }
        //排序
        if (StringUtils.isNoneBlank(sortBy)) {
            String orderByClause = sortBy + (desc ? " desc" : " asc");
            example.setOrderByClause(orderByClause);
        }
        //查询
        List<Brand> brandList = brandMapper.selectByExample(example);
        //解析分页结果
        PageInfo<Brand> info = new PageInfo<>(brandList);
        //判断是否为空
        if (CollectionUtils.isEmpty(brandList)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //返回
        return new PageResult<>(info.getTotal(), info.getPages(), info.getList());
    }

    /**
     * 新增品牌
     *
     * @param brand
     * @param cids
     */
    @Override
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        //新增品牌
        brand.setId(null);
        int count = brandMapper.insert(brand);
        if (count == 0) {
            throw new LyException(ExceptionEnum.BRAND_INSERT_ERROR);
        }
        //新增品牌和分类中间表
        for (Long cid : cids) {
            count = brandMapper.saveCategoryBrand(cid, brand.getId());
            if (count == 0) {
                throw new LyException(ExceptionEnum.BRAND_INSERT_ERROR);
            }
        }
    }

    /**
     * 根据品牌id删除品牌
     *
     * @param bid
     */
    @Override
    @Transactional
    public void deleteByBrandId(Long bid) {
        //删除品牌表
        int count = brandMapper.deleteByPrimaryKey(bid);
        if (count == 0) {
            throw new LyException(ExceptionEnum.BRAND_DELETE_ERROR);
        }
        //删除品牌和分类中间表
        count = brandMapper.deleteByBrandId(bid);
        if (count == 0) {
            throw new LyException(ExceptionEnum.BRAND_DELETE_ERROR);
        }
    }

    /**
     * 根据品牌Id修改品牌
     *
     * @param brand
     * @param cids
     */
    @Override
    @Transactional
    public void updateBrand(Brand brand, List<Long> cids) {
        //修改品牌表
        int count = brandMapper.updateByPrimaryKey(brand);
        if (count == 0) {
            throw new LyException(ExceptionEnum.BRAND_UPDATE_ERROR);
        }
        //删除品牌和分类中间表原数据
        count = brandMapper.deleteByBrandId(brand.getId());
        if (count == 0) {
            throw new LyException(ExceptionEnum.BRAND_UPDATE_ERROR);
        }
        //新增品牌和分类中间表
        for (Long cid : cids) {
            count = brandMapper.saveCategoryBrand(cid, brand.getId());
            if (count == 0) {
                throw new LyException(ExceptionEnum.BRAND_UPDATE_ERROR);
            }
        }
    }

    /**
     * 根据品牌id查询品牌信息
     * @param brandId
     * @return
     */
    @Override
    public Brand queryBrandById(Long brandId) {
        Brand brand = brandMapper.selectByPrimaryKey(brandId);
        if (null == brand) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    /**
     * 根据分类id查询品牌信息
     * @param cid
     * @return
     */
    @Override
    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> list = brandMapper.queryBrandByCid(cid);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return list;
    }

    /**
     * 根据品牌id集合查询品牌
     * @param ids
     * @return
     */
    @Override
    public List<Brand> queryBrandByIds(List<Long> ids) {
        List<Brand> brandList = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(brandList)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brandList;
    }
}
