package com.leyou.common.enums;

import lombok.Getter;

@Getter
public enum ExceptionEnum {
    //不同的枚举之间用逗号隔开,最后一个枚举用分号隔开
    PRICE_CANNOT_BE_NULL(400, "价格不能为空!"),
    CATEGORY_NOT_FOUND(400, "商品分类没有查到!"),
    BRAND_NOT_FOUND(400, "品牌没有查到!"),
    BRAND_INSERT_ERROR(400, "品牌新增失败!"),
    BRAND_DELETE_ERROR(500, "品牌删除失败!"),
    BRAND_UPDATE_ERROR(500, "品牌修改失败!"),
    SPEC_GROUP_NOT_FOUND(404, "商品规格组不存在"),
    SPEC_PARAM_NOT_FOUND(404, "商品规格参数不存在"),
    GROUP_NOT_FOUND(400, "分组没有查到!"),
    GROUP_INSERT_ERROR(400, "分组新增失败!"),
    GROUP_UPDATE_ERROR(500, "分组修改失败!"),
    GROUP_DELETE_ERROR(500, "分组删除失败!"),
    GOODS_NOT_FOUND(400, "商品没有查到!"),
    GOODS_SKU_NOT_FOUND(404,"sku没有找到"),
    GOODS_STOCK_NOT_FOUND(404, "商品库存不存在"),
    GOODS_INSERT_ERROR(400, "商品新增失败!"),
    GOODS_UPDATE_ERROR(500, "商品更新失败!"),
    GOODS_ON_ERROR(500, "商品上架失败!"),
    GOODS_OFF_ERROR(500, "商品下架失败!"),
    GOODS_DELETE_ERROR(500, "商品删除失败!"),
    PARAM_INSERT_ERROR(400, "参数新增失败!"),
    PARAM_DELETE_ERROR(500, "参数删除失败!"),
    PARAM_UPDATE_ERROR(500, "参数修改失败!"),
    INVALID_FILE_TYPE(400, "无效的文件类型!"),
    FILE_UPLOAD_ERROR(500, "文件上传失败!"),
    BAD_REQUEST(400,"请求参数有误!"),
    INVALID_USERNAME_PASSWORD(400,"用户名和密码错误!"),
    UNAUTHORIZED(401,"未登录!"),
    CART_NOT_FOUND(400,"购物车信息未找到!"),
    ADDRESS_NOT_FOUND(400,"收货地址未查到!"),
    STOCK_ERROR(400,"库存不足!"),
    CREATE_ORDER_ERROR(400,"新增订单失败!"),
    ORDER_NOT_FOUND(400,"订单未找到!"),
    INVALID_ORDER_PARAM(400,"无效的订单参数!"),
    UPDATE_ORDER_STATUS_ERROR(400,"修改订单状态错误!"),
    WX_ORDER_ERROR(400,"微信订单错误!"),
    WX_SIGNATURE_INVALID(400,"微信签名无效!"),
    WX_CONNECTION_ERROR(400,"微信连接错误!"),
    ORDER_STATUS_ERROR(400,"订单状态异常!"),
    FAVORITE_ADD_ERROR(400,"新增关注失败!"),
    FAVORITE_NOT_FOUND(400,"我的关注未查到!"),
    ADDRESS_ADD_ERROR(400,"收货地址新增失败!"),
    ;
    private int status;
    private String msg;

    ExceptionEnum(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }
}
