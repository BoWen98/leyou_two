package com.leyou.item.client;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.pojo.Sku;
import com.leyou.pojo.Spu;
import com.leyou.pojo.SpuDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient("item-service")
public interface GoodsClient {

    /**
     * 根据条件分页查询商品信息的接口
     *
     * @param page
     * @param rows
     * @param key
     * @param saleable
     * @return
     */
    @GetMapping("/spu/page")
    PageResult<Spu> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable);

    /**
     * 根据spuId查询spuDetail的接口
     *
     * @param spuId
     * @return
     */
    @GetMapping("/spu/detail/{spuId}")
    SpuDetail querySpuDetailBySpuId(@PathVariable("spuId") Long spuId);

    /**
     * 根据spuId查询sku集合的接口
     *
     * @param spuId
     * @return
     */
    @GetMapping("/sku/list/{spuId}")
    List<Sku> querySkuBySpuId(@PathVariable("spuId") Long spuId);

    /**
     * 根据spuId查询spu
     *
     * @param spuId
     * @return
     */
    @GetMapping("spu/{spuId}")
    Spu querySpuBySpuId(@PathVariable("spuId") Long spuId);

    /**
     * 查询所有spu信息
     *
     * @return
     */
    @GetMapping("spu")
    List<Spu> queryAllSpuId();

    /**
     * 根据skuId集合查询sku信息
     *
     * @param ids
     * @return
     */
    @GetMapping("sku/list/ids")
    List<Sku> querySkuByIds(@RequestParam("ids") List<Long> ids);

    /**
     * 根据skuId查询sku信息
     *
     * @param id
     * @return
     */
    @PostMapping("sku/id")
    Sku querySkuById(@RequestParam("id") Long id);

    /**
     * 订单支付超时,取消订单后把减掉的库存加回来
     *
     * @param cartDTOS
     */
    @PostMapping("stock/decrease")
    void decreaseStock(@RequestBody List<CartDTO> cartDTOS);

    /**
     * 加库存
     * @param
     * @param
     */
    @PostMapping("stock/crease")
    void creaseStock(@RequestBody Map<Long, Integer> map);
}
