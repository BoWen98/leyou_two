package com.leyou.item.controller;

import com.leyou.item.service.CategoryService;
import com.leyou.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父节点查询分类信息
     * @param pid
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryByParentId(@RequestParam(value = "pid", defaultValue = "0") Long pid) {
        return ResponseEntity.ok(categoryService.queryListByParent(pid));
    }

    /**
     * 查询所有一,二,三级分类信息
     * @return
     */
    @GetMapping("listAll")
    public ResponseEntity<List<Category>> queryAllCategory() {
        return ResponseEntity.ok(categoryService.queryAllCategory());
    }


    /**
     * 通过品牌id查询商品分类
     * @param bid
     * @return
     */
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryByBrandId(@PathVariable("bid") Long bid) {
        List<Category> categoryList=categoryService.queryByBrandId(bid);
        return ResponseEntity.ok(categoryList);
    }

    /**
     * 根据分类ids查询分类信息
     * @param ids
     * @return
     */
    @GetMapping("list/ids")
    public ResponseEntity<List<Category>> queryCategoryByIds(@RequestParam("ids") List<Long> ids) {
        List<Category> categoryList = categoryService.queryCategoryByIds(ids);
        return ResponseEntity.ok(categoryList);
    }

    /**
     * 查询一级分类信息
     * @return
     */
    @GetMapping("oneTitle")
    public ResponseEntity<List<Category>> queryOneCategory(){
        List<Category> categoryList =categoryService.queryOneCategory();
        return ResponseEntity.ok(categoryList);
    }

    /**
     * 查询二级分类信息及其下的三级分类信息
     * @return
     */
    @GetMapping("twoTitle")
    public ResponseEntity<Map<Category,List<Category>>> queryTwoCategory(@RequestParam("cid")Long cid){
        Map<Category,List<Category>> categoryList =categoryService.queryTwoCategory(cid);
        return ResponseEntity.ok(categoryList);
    }
 }
