package com.leyou.item.service.impl;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.service.SpecificationService;
import com.leyou.pojo.SpecParam;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.pojo.SpecGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    /**
     * 根据分类id查询参数信息
     * @param cid
     * @return
     */
    @Override
    public List<SpecGroup> querySpecGroupsByCid(Long cid) {
        SpecGroup group = new SpecGroup();
        group.setCid(cid);
        List<SpecGroup> list = specGroupMapper.select(group);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.GROUP_NOT_FOUND);
        }
        return list;

    }

    /**
     * 根据条件查询参数信息
     *
     * @param cid
     * @param gid
     * @param searching
     * @return
     */
    @Override
    public List<SpecParam> querySpecParam(Long cid, Long gid, Boolean searching) {
        SpecParam param = new SpecParam();
        param.setCid(cid);
        param.setGroupId(gid);
        param.setSearching(searching);
        List<SpecParam> list = specParamMapper.select(param);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.GROUP_NOT_FOUND);
        }
        return list;
    }

    /**
     * 根据cid查询分组和参数信息
     * @param cid
     * @return
     */
    @Override
    public List<SpecGroup> querySpecsByCid(Long cid) {
        //查询规格组
        List<SpecGroup> specGroups = querySpecGroupsByCid(cid);
        //查询该cid下的所有参数,并根据groupId进行分组
        List<SpecParam> specParams = querySpecParam(cid, null, null);
        Map<Long, List<SpecParam>> map = specParams.stream().collect(Collectors.groupingBy(SpecParam::getGroupId));
        //封装specGroup中的SpecParam信息
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(map.get(specGroup.getId()));
        }
        return specGroups;
    }

    /**
     * 新增参数分组信息
     *
     * @param specGroup
     */
    @Override
    public void addSpecGroup(SpecGroup specGroup) {
        int count = specGroupMapper.insert(specGroup);
        if (count == 0) {
            throw new LyException(ExceptionEnum.GROUP_INSERT_ERROR);
        }
    }

    /**
     * 修改分组信息
     *
     * @param specGroup
     */
    @Override
    public void updateSpecGroup(SpecGroup specGroup) {
        int count = specGroupMapper.updateByPrimaryKey(specGroup);
        if (count == 0) {
            throw new LyException(ExceptionEnum.GROUP_UPDATE_ERROR);
        }
    }

    /**
     * 删除参数分组
     *
     * @param gid
     */
    @Override
    public void deleteSpecGroup(Long gid) {
        int count = specGroupMapper.deleteByPrimaryKey(gid);
        if (count == 0) {
            throw new LyException(ExceptionEnum.GROUP_DELETE_ERROR);
        }
    }

    /**
     * 新增参数param
     *
     * @param specParam
     */
    @Override
    public void addSpecParam(SpecParam specParam) {
        int count = specParamMapper.insert(specParam);
        if (count == 0) {
            throw new LyException(ExceptionEnum.PARAM_INSERT_ERROR);
        }
    }

    /**
     * 修改参数param
     *
     * @param specParam
     */
    @Override
    public void updateSpecParam(SpecParam specParam) {
        int count = specParamMapper.updateByPrimaryKey(specParam);
        if (count == 0) {
            throw new LyException(ExceptionEnum.PARAM_UPDATE_ERROR);
        }
    }

    /**
     * 删除参数param
     *
     * @param id
     */
    @Override
    public void deleteSpecParam(Long id) {
        int count = specParamMapper.deleteByPrimaryKey(id);
        if (count == 0) {
            throw new LyException(ExceptionEnum.PARAM_DELETE_ERROR);
        }
    }
}
