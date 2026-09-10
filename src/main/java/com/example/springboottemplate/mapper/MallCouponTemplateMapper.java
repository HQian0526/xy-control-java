package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.MallCouponTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MallCouponTemplateMapper extends BaseMapper<MallCouponTemplate> {
    void addMallCouponTemplate(MallCouponTemplate record);

    List<MallCouponTemplate> findMallCouponTemplate(MallCouponTemplate record);

    MallCouponTemplate selectTemplateById(@Param("id") Long id);

    List<MallCouponTemplate> selectClaimableByStoreId(@Param("storeId") Long storeId, @Param("now") java.util.Date now);

    void updateMallCouponTemplate(MallCouponTemplate record);

    int increaseIssuedCount(@Param("id") Long id, @Param("totalCount") Integer totalCount);
}
