package com.leyou.order.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.order.pojo.Order;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrderMapper extends BaseMapper<Order> {

    @Select("select o.* from tb_order o left join tb_order_status s on o.order_id = s.order_id " +
            "where o.user_id = #{userId} and s.status = #{status} limit #{startIndex},#{rows}")
    List<Order> selectOrderByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status, @Param("startIndex") Integer startIndex, @Param("rows") Integer rows);

    @Select("select * from tb_order where user_id=#{userId} limit #{startIndex},#{rows}")
    List<Order> selectOrderByUserId(@Param("userId") Long userId, @Param("startIndex") Integer startIndex, @Param("rows") Integer rows);

    @Select("select distinct o.* from tb_order o left join tb_order_status s on o.order_id = s.order_id left join tb_order_detail d on d.order_id=o.order_id " +
            "where o.user_id = #{userId} and s.status = #{status} and d.title like '%${key}%' limit #{startIndex},#{rows}")
    List<Order> selectOrderByUserIdAndStatusAndKey(@Param("userId") Long userId, @Param("status") String status, @Param("key") String key, @Param("startIndex") Integer startIndex, @Param("rows") Integer rows);

    @Select("select distinct o.* from tb_order o left join tb_order_detail d on d.order_id = o.order_id " +
            "where o.user_id = #{userId} and d.title like '%${key}%' limit #{startIndex},#{rows}")
    List<Order> selectOrderByUserIdAndKey(@Param("userId") Long userId, @Param("key") String key, @Param("startIndex") Integer startIndex, @Param("rows") Integer rows);
}
