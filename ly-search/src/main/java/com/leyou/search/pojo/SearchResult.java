package com.leyou.search.pojo;

import com.leyou.common.vo.PageResult;
import com.leyou.pojo.Brand;
import com.leyou.pojo.Category;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SearchResult extends PageResult<Goods> {

    private List<Category> categories; // 分类待选项

    private List<Brand> brands; // 品牌待选项

    private List<Map<String, Object>> specs; // 规格参数 key以及待选项

    private List<Map<String, Object>> filterList;

    public SearchResult() {

    }

    public SearchResult(Long total, List<Goods> items, List<Map<String, Object>> filterList) {
        super(total, items);
        this.filterList = filterList;
    }

    public SearchResult(Long total, Integer totalPage, List<Goods> items, List<Map<String, Object>> filterList) {
        super(total, totalPage, items);
        this.filterList = filterList;
    }

    public SearchResult(Long total, Integer totalPage, List<Goods> items, List<Category> categories, List<Brand> brands, List<Map<String, Object>> specs) {
        super(total, totalPage, items);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }
}
