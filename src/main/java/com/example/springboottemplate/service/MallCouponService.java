package com.example.springboottemplate.service;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.MallCouponTemplate;
import com.example.springboottemplate.entity.MallUserCoupon;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface MallCouponService {
    Response addMallCouponTemplate(MallCouponTemplate record, HttpServletRequest request);

    Response findMallCouponTemplate(MallCouponTemplate record, Integer pageNum, Integer pageSize,
                                    HttpServletRequest request);

    Response updateMallCouponTemplate(MallCouponTemplate record, HttpServletRequest request);

    Response deleteMallCouponTemplate(List<Long> idList, HttpServletRequest request);

    MallUserCoupon requireUsableCoupon(Long userCouponId, Long userId, Long storeId);

    boolean lockCoupon(Long userCouponId, Long userId, String orderNo);

    void unlockByOrderNo(String orderNo);

    void markUsedByOrderNo(String orderNo);

    Response listStoreTemplates(Long storeId, HttpServletRequest request);

    Response receiveCoupon(Long templateId, HttpServletRequest request);

    Response findMyCoupons(String tab, Long storeId, Integer pageNum, Integer pageSize, HttpServletRequest request);

    List<MallUserCoupon> listUnusedByUserAndStore(Long userId, Long storeId);

    Response grantCoupon(Long templateId, List<Long> userIds, HttpServletRequest request);
}
