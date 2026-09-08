package com.example.springboottemplate.service;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.StoreBusinessHours;
import com.example.springboottemplate.entity.Store;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface StoreService {
    Response addStore(Store store, HttpServletRequest request);

    Response findStore(Store store, Integer pageNum, Integer pageSize);

    Response updateStore(Store store, HttpServletRequest request);

    // 当前登录商家修改自己的店铺资料（名称/头像/位置，不改手机号）
    Response updateStoreProfile(Store store, HttpServletRequest request);

    // 当前登录商家设置营业时间
    Response updateBusinessHours(StoreBusinessHours hours, HttpServletRequest request);

    Response deleteStore(List<Long> idList);
}
