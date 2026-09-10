package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.MallUserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@Mapper
public interface MallUserCouponMapper extends BaseMapper<MallUserCoupon> {
    void addMallUserCoupon(MallUserCoupon record);

    MallUserCoupon selectCouponById(@Param("id") Long id);

    int countByTemplateAndUser(@Param("templateId") Long templateId, @Param("userId") Long userId);

    java.util.List<MallUserCoupon> findMallUserCoupon(@Param("userId") Long userId,
                                                      @Param("storeId") Long storeId,
                                                      @Param("tab") String tab,
                                                      @Param("now") Date now);

    java.util.List<MallUserCoupon> selectUsableByUserAndStore(@Param("userId") Long userId,
                                                              @Param("storeId") Long storeId,
                                                              @Param("now") Date now);

    int lockCoupon(@Param("id") Long id,
                   @Param("userId") Long userId,
                   @Param("orderNo") String orderNo,
                   @Param("now") Date now);

    int unlockByOrderNo(@Param("orderNo") String orderNo);

    int markUsedByOrderNo(@Param("orderNo") String orderNo, @Param("now") Date now);
}
