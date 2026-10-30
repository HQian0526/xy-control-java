package com.example.springboottemplate.service;

import com.example.springboottemplate.dto.Response;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.Date;

public interface StoreBalanceService {
    Response recharge(Long storeId, BigDecimal amount, HttpServletRequest request);

    Response query(String rechargeNo, HttpServletRequest request);

    Response mockConfirm(String rechargeNo, HttpServletRequest request);

    void applyPaid(String rechargeNo, String transactionId);

    int markStoreSettled(Long storeId, Date settleTime, String settleBy);

    /** 已结算订单再从平台商户号退款时，冲减店铺余额，避免下次结算把这笔已垫付的钱再打给商家。 */
    void addSettledRefundClawback(Long storeId, String outRefundNo, String orderNo, int refundFen);

    int markAdjustSettled(Long storeId, Date settleTime, String settleBy);
}
