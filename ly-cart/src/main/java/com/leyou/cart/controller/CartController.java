package com.leyou.cart.controller;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import com.leyou.cart.pojo.Favorite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 添加商品到购物车
     *
     * @param cart
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart) {
        cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 批量添加商品到购物车
     *
     * @param carts
     * @return
     */
    @PostMapping("list")
    public ResponseEntity<Void> addCart(@RequestBody List<Cart> carts) {
        cartService.addCarts(carts);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 查询购物车商品信息
     *
     * @param token
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Cart>> queryCartList(@CookieValue("LY_TOKEN") String token) {
        return ResponseEntity.ok(cartService.queryCartList());
    }

    /**
     * 修改购物车商品数量
     *
     * @param token
     * @param id
     * @param num
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateCart(@CookieValue("LY_TOKEN") String token,
                                           @RequestParam("id") Long id,
                                           @RequestParam("num") Integer num) {
        cartService.updateCart(id, num);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 删除购物车商品
     *
     * @param token
     * @param id
     * @return
     */
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteCart(@CookieValue("LY_TOKEN") String token,
                                           @PathVariable("id") Long id) {
        cartService.deleteCart(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 添加商品到我的关注
     *
     * @param token
     * @param favorite
     * @return
     */
    @PostMapping("/favorite/")
    public ResponseEntity<Void> addCollection(@CookieValue("LY_TOKEN") String token,
                                              @RequestBody Favorite favorite) {
        cartService.addCollection(favorite);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 查询我的收藏
     *
     * @return
     */
    @GetMapping("/favorite/list")
    public ResponseEntity<List<Favorite>> queryMyFavorite() {
        return ResponseEntity.ok(cartService.queryMyFavorite());
    }
}
