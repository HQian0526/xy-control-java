package com.example.springboottemplate.service;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.mall.MallCheckoutRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface MallOrderService {
    Response checkoutAndPay(MallCheckoutRequest request, HttpServletRequest httpRequest);

    Response queryOrder(String orderNo, HttpServletRequest httpRequest);

    Response findMallOrder(Integer payStatus, Integer pageNum, Integer pageSize, HttpServletRequest httpRequest);

    Response mockConfirmPay(String orderNo, HttpServletRequest httpRequest);

    String handleWxPayNotify(HttpServletRequest request, String body);
}
