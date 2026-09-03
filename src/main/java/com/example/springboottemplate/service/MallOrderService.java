package com.example.springboottemplate.service;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.mall.MallCheckoutRequest;
import com.example.springboottemplate.dto.mall.MallRefundRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface MallOrderService {
    Response checkoutAndPay(MallCheckoutRequest request, HttpServletRequest httpRequest);

    Response queryOrder(String orderNo, HttpServletRequest httpRequest);

    Response findMallOrder(Integer payStatus, Long storeId, Integer pageNum, Integer pageSize,
                           HttpServletRequest httpRequest);

    Response mockConfirmPay(String orderNo, HttpServletRequest httpRequest);

    String handleWxPayNotify(HttpServletRequest request, String body);

    /** 商户/管理员发起退款（全额或部分） */
    Response refund(MallRefundRequest request, HttpServletRequest httpRequest);

    /** 微信退款结果回调 */
    String handleWxRefundNotify(HttpServletRequest request, String body);
}
