package com.leyou.item.service;

import com.leyou.pojo.Category;

import java.util.List;
import java.util.Map;

public interface CategoryService {
    List<Category> queryListByParent(Long pid);

    List<Category> queryByBrandId(Long bid);

    List<Category> queryCategoryByIds(List<Long> ids);

    List<Category> queryAllCategory();

    List<Category> queryOneCategory();

    Map<Category, List<Category>> queryTwoCategory(Long cid);
}
