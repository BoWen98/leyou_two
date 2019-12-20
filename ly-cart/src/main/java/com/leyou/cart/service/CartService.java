package com.leyou.cart.service;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.pojo.Favorite;

import java.util.List;
import java.util.Map;

public interface CartService {
    void addCart(Cart cart);

    List<Cart> queryCartList();

    void updateCart(Long id, Integer num);

    void deleteCart(Long id);

    void addCarts(List<Cart> carts);

    void deleteCarts(Map<Long, List<Long>> map);

    void addCollection(Favorite favorite);

    List<Favorite> queryMyFavorite();
}
