package com.example.springboottemplate.service;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.StoreBlacklist;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface StoreBlacklistService {
    Response addStoreBlacklist(StoreBlacklist record, HttpServletRequest request);

    Response findStoreBlacklist(StoreBlacklist record, Integer pageNum, Integer pageSize, HttpServletRequest request);

    Response updateStoreBlacklist(StoreBlacklist record, HttpServletRequest request);

    Response deleteStoreBlacklist(List<Long> idList, HttpServletRequest request);

    /** 本店下单拦截：优先用户 id，其次账号/联系人手机号 */
    boolean isBlacklisted(Long storeId, Long userId, String... phones);
}
