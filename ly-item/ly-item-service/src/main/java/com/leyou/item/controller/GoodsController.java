package com.leyou.item.controller;

import com.leyou.common.dto.CartDTO;
import com.leyou.pojo.Sku;
import com.leyou.pojo.Spu;
import com.leyou.common.vo.PageResult;
import com.leyou.item.service.GoodsService;
import com.leyou.pojo.SpuDetail;
import com.leyou.pojo.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 分页查询商品信息
     *
     * @param page
     * @param rows
     * @param key
     * @param saleable
     * @return
     */
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<Spu>> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable) {
        return ResponseEntity.ok(goodsService.querySpuByPage(page, rows, key, saleable));
    }

    /**
     * 新增商品
     *
     * @param spu
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody Spu spu) {
        goodsService.saveGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据spuId查询detail
     *
     * @param spuId
     * @return
     */
    @GetMapping("/spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("spuId") Long spuId) {
        return ResponseEntity.ok(goodsService.querySpuDetailBySpuId(spuId));
    }

    /**
     * 根据spuId查询sku
     *
     * @param spuId
     * @return
     */
    @GetMapping("/sku/list/{spuId}")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@PathVariable("spuId") Long spuId) {
        return ResponseEntity.ok(goodsService.querySkuBySpuId(spuId));
    }

    /**
     * 根据sku的id集合查询sku集合
     * @param ids
     * @return
     */
    @GetMapping("sku/list/ids")
    public ResponseEntity<List<Sku>> querySkuByIds(@RequestParam("ids") List<Long> ids) {
        return ResponseEntity.ok(goodsService.querySkuByIds(ids));
    }

    /**
     * 根据skuId查询sku
     * @param id
     * @return
     */
    @PostMapping("sku/id")
    public ResponseEntity<Sku> querySkuById(@RequestParam("id") Long id) {
        return ResponseEntity.ok(goodsService.querySkuById(id));
    }
    /**
     * 根据skuId查询商品库存
     * @param id
     * @return
     */
    @GetMapping("stock/skuId")
    public ResponseEntity<Stock> queryStockById(@RequestParam("skuId") Long id) {
        return ResponseEntity.ok(goodsService.queryStockById(id));
    }
    /**
     * 修改商品
     *
     * @param spu
     * @return
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody Spu spu) {
        goodsService.updateGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 删除商品
     * @param spuId
     * @return
     */
    @DeleteMapping("/goods/{spuId}")
    public ResponseEntity<Void> deleteGoods(@PathVariable("spuId") Long spuId) {
        goodsService.deleteGoods(spuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 上架商品
     * @param spuId
     * @return
     */
    @PutMapping("/goods/on/{spuId}")
    public ResponseEntity<Void> onSaleGoods(@PathVariable("spuId") Long spuId) {
        goodsService.onSaleGoods(spuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 下架商品
     * @param spuId
     * @return
     */
    @PutMapping("/goods/off/{spuId}")
    public ResponseEntity<Void> offSaleGoods(@PathVariable("spuId") Long spuId) {
        goodsService.offSaleGoods(spuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 根据spuId查询spu信息
     * @param spuId
     * @return
     */
    @GetMapping("spu/{spuId}")
    public ResponseEntity<Spu> querySpuBySpuId(@PathVariable("spuId") Long spuId) {
        return ResponseEntity.ok(goodsService.querySpuBySpuId(spuId));
    }

    /**
     * 查询所有商品的spuId集合
     * @return
     */
    @GetMapping("spu")
    public ResponseEntity<List<Spu>> queryAllSpuId() {
        return ResponseEntity.ok(goodsService.queryAllSpuId());
    }

    /**
     * 减库存
     * @param cartDTOS
     */
    @PostMapping("stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDTO> cartDTOS) {
        goodsService.decreaseStock(cartDTOS);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 订单支付超时,取消订单后把减掉的库存加回来
     * @param
     * @param
     */
    @PostMapping("stock/crease")
    public ResponseEntity<Void> creaseStock(@RequestBody Map<Long, Integer> map){
        goodsService.creaseStock(map);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
