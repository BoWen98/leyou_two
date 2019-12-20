package com.leyou.item.service;

import com.leyou.pojo.SpecParam;
import com.leyou.pojo.SpecGroup;

import java.util.List;

public interface SpecificationService {
    List<SpecGroup> querySpecGroupsByCid(Long cid);

    void addSpecGroup(SpecGroup specGroup);

    void updateSpecGroup(SpecGroup specGroup);

    void deleteSpecGroup(Long gid);

    void addSpecParam(SpecParam specParam);

    void updateSpecParam(SpecParam specParam);

    void deleteSpecParam(Long id);

    List<SpecParam> querySpecParam(Long cid, Long gid, Boolean searching);

    List<SpecGroup> querySpecsByCid(Long cid);
}
