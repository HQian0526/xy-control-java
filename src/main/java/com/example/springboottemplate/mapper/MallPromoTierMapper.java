package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.MallPromoTier;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MallPromoTierMapper extends BaseMapper<MallPromoTier> {
    void addMallPromoTier(MallPromoTier record);

    void batchAdd(@Param("list") List<MallPromoTier> list);

    List<MallPromoTier> selectByPromoId(@Param("promoId") Long promoId);

    List<MallPromoTier> selectByPromoIds(@Param("promoIds") List<Long> promoIds);

    void deleteByPromoId(@Param("promoId") Long promoId);
}
