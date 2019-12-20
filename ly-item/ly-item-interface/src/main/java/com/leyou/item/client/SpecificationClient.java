package com.leyou.item.client;

import com.leyou.pojo.SpecGroup;
import com.leyou.pojo.SpecParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "item-service",path = "spec")
public interface SpecificationClient {

    /**
     * 根据条件查询参数信息的接口
     * @param cid
     * @param gid
     * @param searching
     * @return
     */
    @GetMapping("/params")
    List<SpecParam> querySpecParam(@RequestParam(value = "cid", required = false) Long cid,
                                   @RequestParam(value = "gid", required = false) Long gid,
                                   @RequestParam(value = "searching", required = false) Boolean searching);


    /**
     * 根据cid查询分组信息
     * @param cid
     * @return
     */
    @GetMapping("/list/{cid}")
    List<SpecGroup> querySpecsByCid(@PathVariable("cid") Long cid);
}
