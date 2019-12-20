package com.leyou.item.controller;

import com.leyou.item.service.SpecificationService;
import com.leyou.pojo.SpecParam;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.pojo.SpecGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    /**
     * 根据分类id查询分组信息
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecGroups(@PathVariable("cid")Long cid) {
        List<SpecGroup> list=specificationService.querySpecGroupsByCid(cid);
        if (null == list) {
            throw new LyException(ExceptionEnum.GROUP_NOT_FOUND);
        }
        return ResponseEntity.ok(list);
    }

    /**
     * 根据条件查询参数
     * @param cid
     * @param gid
     * @param searching
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> querySpecParam(@RequestParam(value = "cid",required = false) Long cid,
                                                          @RequestParam(value = "gid",required = false) Long gid,
                                                          @RequestParam(value = "searching",required = false) Boolean searching) {
        List<SpecParam> list = specificationService.querySpecParam(cid,gid,searching);
        return ResponseEntity.ok(list);
    }

    /**
     * 新增分组信息
     * @param specGroup
     * @return
     */
    @PostMapping("group")
    public ResponseEntity<Void> addSpecGroup(@RequestBody SpecGroup specGroup) {
        specificationService.addSpecGroup(specGroup);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 修改分组信息
     * @param specGroup
     * @return
     */
    @PutMapping("group")
    public ResponseEntity<Void> updateSpecGroup(@RequestBody SpecGroup specGroup) {
        specificationService.updateSpecGroup(specGroup);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 删除分组信息
     * @param gid
     * @return
     */
    @DeleteMapping("group/{gid}")
    public ResponseEntity<Void> deleteSpecGroup(@PathVariable("gid")Long gid) {
        specificationService.deleteSpecGroup(gid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 新增参数信息
     * @param specParam
     * @return
     */
    @PostMapping("param")
    public ResponseEntity<Void> addSpecParam(@RequestBody SpecParam specParam) {
        specificationService.addSpecParam(specParam);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 修改参数信息
     * @param specParam
     * @return
     */
    @PutMapping("param")
    public ResponseEntity<Void> updateSpecParam(@RequestBody SpecParam specParam) {
        specificationService.updateSpecParam(specParam);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 删除参数信息
     * @param id
     * @return
     */
    @DeleteMapping("param/{id}")
    public ResponseEntity<Void> deleteSpecParam(@PathVariable("id")Long id) {
        specificationService.deleteSpecParam(id);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 根据cid查询分组信息
     * @param cid
     * @return
     */
    @GetMapping("list/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecsByCid(@PathVariable("cid") Long cid) {
        return ResponseEntity.ok(specificationService.querySpecsByCid(cid));
    }
}
