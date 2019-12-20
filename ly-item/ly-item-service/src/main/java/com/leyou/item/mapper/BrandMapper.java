package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.pojo.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BrandMapper extends BaseMapper<Brand> {

    /**
     * 新增'商品分类和品牌中间表'数据
     *
     * @param cid
     * @param bid
     * @return
     */
    @Insert("insert into tb_category_brand(category_id,brand_id) values(#{cid},#{bid})")
    int saveCategoryBrand(@Param("cid") Long cid, @Param("bid") Long bid);

    /**
     * 删除'商品分类和品牌中间表'数据
     *
     * @param bid
     * @return
     */
    @Delete("delete from tb_category_brand where brand_id = #{bid}")
    int deleteByBrandId(Long bid);

    @Select("select * from tb_brand where id in(select brand_id from tb_category_brand where category_id= #{cid})")
    List<Brand> queryBrandByCid(Long cid);
}
