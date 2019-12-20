package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.service.BrandService;
import com.leyou.item.service.CategoryService;
import com.leyou.item.service.GoodsService;
import com.leyou.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 分页查询商品信息
     *
     * @param page
     * @param rows
     * @param key
     * @param saleable
     * @return
     */
    @Override
    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, String key, Boolean saleable) {
        //1.分页
        PageHelper.startPage(page, rows);
        //2.过滤条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        //2.1模糊查询
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        //2.2上下架过滤
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        //2.3是否被逻辑删除过滤
        criteria.andEqualTo("valid", true);

        //3.按照更新时间排序
        example.setOrderByClause("last_update_time desc");

        //4.查询
        List<Spu> spuList = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(spuList)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //5.处理分类和品牌的名称
        handleCategoryAndBrandName(spuList);//调用下面处理的方法

        //6.解析查询结果并返回
        PageInfo<Spu> info = new PageInfo<>(spuList);
        return new PageResult<>(info.getTotal(), info.getList());
    }

    /**
     * 处理分类和品牌名称的方法
     *
     * @param spus
     */
    private void handleCategoryAndBrandName(List<Spu> spus) {
        for (Spu spu : spus) {
            //处理品牌名称
            spu.setBname(brandService.queryBrandById(spu.getBrandId()).getName());
            //处理分类id
            List<String> names = categoryService.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names, "/"));
        }
    }

    /**
     * 新增商品
     *
     * @param spu
     */
    @Override
    @Transactional
    public void saveGoods(Spu spu) {
        //新增spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(false);
        spu.setValid(true);
        int count = spuMapper.insertSelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_INSERT_ERROR);
        }
        //新增spu_details
        SpuDetail detail = spu.getSpuDetail();
        detail.setSpuId(spu.getId());
        count = spuDetailMapper.insertSelective(detail);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_INSERT_ERROR);
        }
        //新增sku和stock
        saveSkuAndStock(spu);//调用下面定义好的方法

    }

    /**
     * 根据spuId查询detail
     *
     * @param spuId
     * @return
     */
    @Override
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        SpuDetail detail = spuDetailMapper.selectByPrimaryKey(spuId);
        if (null == detail) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return detail;
    }

    /**
     * 根据spuId查询sku集合
     *
     * @param spuId
     * @return
     */
    @Override
    public List<Sku> querySkuBySpuId(Long spuId) {
        //查询sku集合
        Sku s = new Sku();
        s.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(s);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        //获取sku的id
        List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());

        //查询库存
        List<Stock> stockList = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stockList)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //把库存值集合编程一个map,key是skuId,值是库存的值
        Map<Long, Integer> stockMap = stockList.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        for (Sku sku : skuList) {
            sku.setStock(stockMap.get(sku.getId()));
        }
        return skuList;
    }

    /**
     * 修改商品
     *
     * @param spu
     */
    @Override
    @Transactional
    public void updateGoods(Spu spu) {
        //对要修改的商品id进行判空
        if (null == spu.getId()) {
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        //因为sku和stock都属于不固定信息,所以没办法直接修改,只能把原来数据删除,然后把新数据添加进去
        //查询原来的sku
        Sku s = new Sku();
        s.setSpuId(spu.getId());
        List<Sku> skuList = skuMapper.select(s);
        if (!CollectionUtils.isEmpty(skuList)) {
            //删除以前的sku
            int count = skuMapper.delete(s);
            if (count != skuList.size()) {
                throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
            }
            //删除以前的stock
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            count = stockMapper.deleteByIdList(ids);
            if (count != skuList.size()) {
                throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
            }
            //修改spu
            //以下几个字段不修改(设置为null之后,使用通用mapper的updateSelective方法默认不更新)
            spu.setValid(null);
            spu.setSaleable(null);
            spu.setCreateTime(null);
            spu.setLastUpdateTime(new Date());
            count = spuMapper.updateByPrimaryKeySelective(spu);
            if (count != 1) {
                throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
            }
            //修改detail
            SpuDetail detail = spu.getSpuDetail();
            count = spuDetailMapper.updateByPrimaryKeySelective(detail);
            if (count != 1) {
                throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
            }
            //新增sku和stock
            saveSkuAndStock(spu);//调用下面定义好的方法
        }
    }

    /**
     * 删除商品(逻辑删除)
     *
     * @param spuId
     */
    @Override
    public void deleteGoods(Long spuId) {
        int count = spuMapper.updateSpuValid(spuId);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_DELETE_ERROR);
        }
    }

    /**
     * 上架商品
     *
     * @param spuId
     */
    @Override
    public void onSaleGoods(Long spuId) {
        int count = spuMapper.updateSpuSaleableOn(spuId);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_ON_ERROR);
        }
        //发送消息
        sendMessage(spuId, "insert");
    }

    /**
     * 下架商品
     *
     * @param spuId
     */
    @Override
    public void offSaleGoods(Long spuId) {
        int count = spuMapper.updateSpuSaleableOff(spuId);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_OFF_ERROR);
        }
        //发送消息
        sendMessage(spuId, "delete");
    }

    //新增sku和stock的方法
    private void saveSkuAndStock(Spu spu) {
        //新增sku
        int count;
        List<Sku> skuList = spu.getSkus();
        //创建集合,记录sku的库存
        List<Stock> stockList = new ArrayList<>();
        for (Sku sku : skuList) {
            //填写sku信息
            sku.setSpuId(spu.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            count = skuMapper.insert(sku);
            System.out.println(sku);
            if (count != 1) {
                throw new LyException(ExceptionEnum.GOODS_INSERT_ERROR);
            }
            //初始化stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockList.add(stock);
        }
        //新增stock
        count = stockMapper.insertList(stockList);

        if (count != stockList.size()) {
            throw new LyException(ExceptionEnum.GOODS_INSERT_ERROR);
        }
    }

    /**
     * 根据spuId查询spu信息,里面还包含spuDetail和sku信息
     *
     * @param spuId
     * @return
     */
    @Override
    public Spu querySpuBySpuId(Long spuId) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if (null == spu) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //查询spuDetail
        spu.setSpuDetail(querySpuDetailBySpuId(spuId));
        //查询sku的集合
        spu.setSkus(querySkuBySpuId(spuId));
        return spu;
    }

    /**
     * 查询所有商品spuId集合
     *
     * @return
     */
    @Override
    public List<Spu> queryAllSpuId() {
        List<Spu> spuList = spuMapper.selectAll();
        if (CollectionUtils.isEmpty(spuList)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return spuList;
    }

    /**
     * 根据sku的id集合查询sku集合
     *
     * @param ids
     * @return
     */
    @Override
    public List<Sku> querySkuByIds(List<Long> ids) {
        //查询sku
        List<Sku> skuList = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //填充库存
        fillStock(ids, skuList);
        return skuList;
    }

    /**
     * 根据skuId查询商品库存
     *
     * @param id
     * @return
     */
    @Override
    public Stock queryStockById(Long id) {
        Stock s = new Stock();
        s.setSkuId(id);
        Stock stock = stockMapper.selectOne(s);
        if (null == stock) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return stock;
    }


    public void fillStock(List<Long> ids, List<Sku> skus) {
        //查询库存
        List<Stock> stockList = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stockList)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //把库存转为map,key是skuId,值是库存
        Map<Long, Integer> stockMap = stockList.stream().collect((Collectors.toMap(s -> s.getSkuId(), s -> s.getStock())));

        //保存库存到sku
        for (Sku sku : skus) {
            sku.setStock(stockMap.get(sku.getId()));
        }
    }

    /**
     * 商品新增时向rabbitmq发送消息
     *
     * @param id
     * @param type
     */
    public void sendMessage(Long id, String type) {
        try {
            this.amqpTemplate.convertAndSend("item." + type, id);
        } catch (Exception e) {
            log.error("【商品服务】商品消息发送异常，商品id：{}", id, type, e);
        }
    }

    /**
     * 减库存
     *
     * @param cartDTOS
     */
    @Override
    public void decreaseStock(List<CartDTO> cartDTOS) {
        for (CartDTO cartDTO : cartDTOS) {
            int count = stockMapper.decreaseStock(cartDTO.getSkuId(), cartDTO.getNum());
            if (count != 1) {
                throw new LyException(ExceptionEnum.STOCK_ERROR);
            }
        }
    }

    /**
     * 根据skuId查询sku
     *
     * @param id
     * @return
     */
    @Override
    public Sku querySkuById(Long id) {
        Sku sku = skuMapper.selectByPrimaryKey(id);
        if (null == sku) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return sku;
    }

    /**
     * 订单支付超时,取消订单后把减掉的库存加回来
     *
     * @param map
     */
    @Override
    public void creaseStock(Map<Long, Integer> map) {
        Set<Long> longs = map.keySet();
        for (Long skuId : longs) {
            Integer num = map.get(skuId);
            int count = stockMapper.creaseStock(skuId, num);
            if (count != 1) {
                throw new LyException(ExceptionEnum.STOCK_ERROR);
            }
        }
    }
}
