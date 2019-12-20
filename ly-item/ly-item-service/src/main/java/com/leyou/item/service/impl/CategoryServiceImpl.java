package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.service.CategoryService;
import com.leyou.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class CategoryServiceImpl implements CategoryService {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 查询分类信息
     *
     * @param pid
     * @return
     */
    public List<Category> queryListByParent(Long pid) {
        //查询条件,mapper会把对象中的非空属性作为查询条件
        Category category = new Category();
        category.setParentId(pid);
        List<Category> categoryList = categoryMapper.select(category);
        //判断结果
        if (CollectionUtils.isEmpty(categoryList)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return categoryList;
    }

    /**
     * 根据品牌id查询分类信息
     *
     * @param bid
     * @return
     */
    @Override
    public List<Category> queryByBrandId(Long bid) {
        List<Category> categoryList = categoryMapper.queryByBrandId(bid);
        if (CollectionUtils.isEmpty(categoryList)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return categoryList;
    }

    /**
     * 根据ids查询分类信息
     *
     * @param ids
     * @return
     */
    @Override
    public List<Category> queryCategoryByIds(List<Long> ids) {
        List<Category> categoryList = categoryMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(categoryList)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return categoryList;
    }

    /**
     * 查询所有一,二,三级分类信息
     *
     * @return
     */
    @Override
    public List<Category> queryAllCategory() {
        List<Category> categoryList = categoryMapper.selectAll();
        if (CollectionUtils.isEmpty(categoryList)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return categoryList;
    }

    /**
     * 查询一级分类信息
     *
     * @return
     */
    @Override
    public List<Category> queryOneCategory() {
        PageHelper.startPage(0, 15);
        Example example = new Example(Category.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("parentId", 0L);
        example.setOrderByClause("sort asc");
        List<Category> categories = categoryMapper.selectByExample(example);
        return categories;
    }

    /**
     * 查询二级分类信息及其下的三级分类信息
     *
     * @return
     */
    @Override
    public Map<Category, List<Category>> queryTwoCategory(Long cid) {
        Example example = new Example(Category.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("parentId", cid);
        example.setOrderByClause("sort asc");
        List<Category> twoCategories = categoryMapper.selectByExample(example);
        Map<Category, List<Category>> map = new HashMap<>();
        for (Category twoCategory : twoCategories) {
            map.put(twoCategory, twoCategories);
            Long id = twoCategory.getId();
            Example example1 = new Example(Category.class);
            Example.Criteria criteria1 = example1.createCriteria();
            criteria1.andEqualTo("parentId", id);
            example1.setOrderByClause("sort asc");
            List<Category> threeCategories = categoryMapper.selectByExample(example1);
            map.put(twoCategory, threeCategories);
        }
        return map;
    }
}
