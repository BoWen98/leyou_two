package com.leyou.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {// data transfer object
    private Long addressId; // 收获人地址id
    private Integer paymentType;// 付款类型
    private List<CartDTO> carts;// 订单详情
}