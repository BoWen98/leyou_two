package com.leyou.item.mapper;

import com.leyou.pojo.Spu;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface SpuMapper extends Mapper<Spu> {
    @Update("update tb_spu set valid=0 where id=#{spuId}")
    int updateSpuValid(Long spuId);

    @Update("update tb_spu set saleable=1 where id=#{spuId}")
    int updateSpuSaleableOn(Long spuId);

    @Update("update tb_spu set saleable=0 where id=#{spuId}")
    int updateSpuSaleableOff(Long spuId);
}
