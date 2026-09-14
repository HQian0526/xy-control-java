package com.example.springboottemplate.service;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.mall.StoreMemberRechargeRequest;
import com.example.springboottemplate.dto.mall.StoreMemberStatusRequest;
import com.example.springboottemplate.entity.StoreMember;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.Date;

public interface StoreMemberService {
    Response mine(Long storeId, HttpServletRequest request);

    Response recharge(StoreMemberRechargeRequest request, HttpServletRequest httpRequest);

    Response queryRecharge(String rechargeNo, HttpServletRequest httpRequest);

    Response mockConfirmRecharge(String rechargeNo, HttpServletRequest httpRequest);

    Response updateStatus(StoreMemberStatusRequest request, HttpServletRequest httpRequest);

    void applyRechargePaid(String rechargeNo, String transactionId);

    StoreMember find(Long storeId, Long userId);

    boolean debit(Long storeId, Long userId, BigDecimal amount);

    void credit(Long storeId, Long userId, BigDecimal amount);

    int closeExpiredUnpaid(Date expireBefore, int limit);

    int markStoreSettled(Long storeId, Date settleTime, String settleBy);
}
