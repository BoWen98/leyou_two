package com.leyou.item.service;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.pojo.Sku;
import com.leyou.pojo.Spu;
import com.leyou.pojo.SpuDetail;
import com.leyou.pojo.Stock;

import java.util.List;
import java.util.Map;

public interface GoodsService {
    PageResult<Spu> querySpuByPage(Integer page, Integer rows, String key, Boolean saleable);

    void saveGoods(Spu spu);

    SpuDetail querySpuDetailBySpuId(Long spuId);

    List<Sku> querySkuBySpuId(Long spuId);

    void updateGoods(Spu spu);

    void deleteGoods(Long spuId);

    void onSaleGoods(Long spuId);

    void offSaleGoods(Long spuId);

    Spu querySpuBySpuId(Long spuId);

    List<Spu> queryAllSpuId();

    List<Sku> querySkuByIds(List<Long> ids);

    Stock queryStockById(Long id);

    void decreaseStock(List<CartDTO> cartDTOS);

    Sku querySkuById(Long id);

    void creaseStock(Map<Long, Integer> map);
}
