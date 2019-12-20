package com.leyou.cart.service.impl;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.Interceptors.UserInterceptor;
import com.leyou.cart.mapper.FavoriteMapper;
import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.client.GoodsClient;
import com.leyou.cart.pojo.Favorite;
import com.leyou.pojo.Sku;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "cart:uid:";

    /**
     * 添加商品到购物车
     *
     * @param cart
     */
    @Override
    public void addCart(Cart cart) {
        //获取登录的用户信息
        UserInfo loginUser = UserInterceptor.getLoginUser();
        // 准备key
        String key = KEY_PREFIX + loginUser.getId();
        // 获取operation对象,并且绑定key，剩下的就是操作map
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);
        // 准备hashKey，也就是商品id
        String hKey = cart.getSkuId().toString();
        // 判断在购物车是否存在
        if (hashOps.hasKey(hKey)) {
            //存在则先取出
            Cart cacheCart = JsonUtils.toBean(hashOps.get(hKey), Cart.class);
            // 修改数量
            cart.setNum(cacheCart.getNum() + cart.getNum());
        }
        // 写回redis中
        hashOps.put(hKey, JsonUtils.toString(cart));
    }

    /**
     * 查询购物车
     *
     * @return
     */
    @Override
    public List<Cart> queryCartList() {
        //获取登录的user信息
        UserInfo loginUser = UserInterceptor.getLoginUser();
        // 准备key
        String key = KEY_PREFIX + loginUser.getId();
        // 先判断是否存在用户的信息
        if (redisTemplate.hasKey(key) == null || !redisTemplate.hasKey(key)) {
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        // 获取operation对象,并且绑定key，剩下的就是操作map
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);
        // 查询用户的所有购物车
        List<String> list = hashOps.values();
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        // 购物车有数据，转为Cart对象
        List<Cart> cartList = list.stream().map(v -> JsonUtils.toBean(v, Cart.class)).collect(Collectors.toList());
        return cartList;
    }

    /**
     * 修改购物车商品数量
     *
     * @param id
     * @param num
     */
    @Override
    public void updateCart(Long id, Integer num) {
        //获取登录的user信息
        UserInfo loginUser = UserInterceptor.getLoginUser();
        // 准备key
        String key = KEY_PREFIX + loginUser.getId();
        // 获取operation对象,并且绑定key，剩下的就是操作map
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);
        //判断购物中该商品是否存在
        if (hashOps.hasKey(id.toString())) {
            //存在则先取出
            Cart cacheCart = JsonUtils.toBean(hashOps.get(id.toString()), Cart.class);
            // 修改数量
            cacheCart.setNum(num);
            // 写回redis中
            hashOps.put(id.toString(), JsonUtils.toString(cacheCart));
        } else {
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
    }

    /**
     * 删除购物车商品
     *
     * @param id
     */
    @Override
    public void deleteCart(Long id) {
        //获取登录的user信息
        UserInfo loginUser = UserInterceptor.getLoginUser();
        // 准备key
        String key = KEY_PREFIX + loginUser.getId();
        // 获取operation对象,并且绑定key，剩下的就是操作map
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);
        //判断购物中该商品是否存在
        if (hashOps.hasKey(id.toString())) {
            //存在则删除
            hashOps.delete(id.toString());
        } else {
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
    }

    /**
     * 批量添加购物车商品
     *
     * @param carts
     */
    @Override
    public void addCarts(List<Cart> carts) {
        //获取登录的用户信息
        UserInfo loginUser = UserInterceptor.getLoginUser();
        // 准备key
        String key = KEY_PREFIX + loginUser.getId();
        // 获取operation对象,并且绑定key，剩下的就是操作map
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);
        for (Cart cart : carts) {
            // 准备hashKey，也就是商品id
            String hKey = cart.getSkuId().toString();
            if (hashOps.hasKey(hKey)) {
                //存在则先取出
                Cart cacheCart = JsonUtils.toBean(hashOps.get(hKey), Cart.class);
                // 修改数量
                cart.setNum(cacheCart.getNum() + cart.getNum());
            }
            // 写回redis中
            hashOps.put(hKey, JsonUtils.toString(cart));
        }
    }

    /**
     * 购物车中商品购买后删除
     *
     * @param map
     */
    @Override
    public void deleteCarts(Map<Long, List<Long>> map) {
        // 准备key
        String key = "";
        for (Long s : map.keySet()) {
            key = KEY_PREFIX + s;
        }
        // 获取operation对象,并且绑定key，剩下的就是操作map
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);
        for (List<Long> value : map.values()) {
            for (Long hkey : value) {
                //判断购物中该商品是否存在
                if (hashOps.hasKey(hkey.toString())) {
                    //存在则删除
                    hashOps.delete(hkey.toString());
                } else {
                    throw new LyException(ExceptionEnum.CART_NOT_FOUND);
                }
            }
        }
    }

    /**
     * 根据skuId查询是否存在这个商品
     * @param id
     * @return
     */
    public Boolean queryCartById(Long id) {
        //获取登录的user信息
        UserInfo loginUser = UserInterceptor.getLoginUser();
        // 准备key
        String key = KEY_PREFIX + loginUser.getId();
        // 获取operation对象,并且绑定key，剩下的就是操作map
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);
        //判断购物中该商品是否存在
        if (hashOps.hasKey(id.toString())) {
            //存在则返回true
            return true;
        } else {
            //不存在返回false
            return false;
        }
    }

    /**
     * 移除购物车商品到我的关注
     *
     * @param favorite
     */
    @Override
    @Transactional
    public void addCollection(Favorite favorite) {
        //获取登录的user信息
        UserInfo loginUser = UserInterceptor.getLoginUser();
        //查询sku信息
        favorite.setUserId(loginUser.getId());
        int count = favoriteMapper.insert(favorite);
        if (count != 1) {
            throw new LyException(ExceptionEnum.FAVORITE_ADD_ERROR);
        }
        Boolean flag = queryCartById(favorite.getSkuId());
        if (flag) {
            deleteCart(favorite.getSkuId());
        }
    }

    /**
     * 查询我的收藏
     * @return
     */
    @Override
    public List<Favorite> queryMyFavorite() {
        //获取登录的user信息
        UserInfo loginUser = UserInterceptor.getLoginUser();
        Long userId = loginUser.getId();
        Favorite f = new Favorite();
        f.setUserId(userId);
        List<Favorite> favoriteList = favoriteMapper.select(f);
        if (CollectionUtils.isEmpty(favoriteList)) {
            throw new LyException(ExceptionEnum.FAVORITE_NOT_FOUND);
        }
        return favoriteList;
    }
}
