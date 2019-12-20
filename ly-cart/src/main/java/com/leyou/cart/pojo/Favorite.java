package com.leyou.cart.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_collections")
public class Favorite {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    private Long skuId;//商品id
    private Long spuId;//spuId
    private Long userId;//收藏用户id
    private String title;//标题
    private String image;//图片
    private Long price;//价格
    private String ownSpec;//商品规格参数
}
