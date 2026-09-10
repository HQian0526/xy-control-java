package com.example.springboottemplate.service;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.mall.MallCheckoutRequest;
import com.example.springboottemplate.dto.mall.MallRefundRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface MallOrderService {
    Response checkoutAndPay(MallCheckoutRequest request, HttpServletRequest httpRequest);

    Response queryOrder(String orderNo, HttpServletRequest httpRequest);

    Response findMallOrder(Integer payStatus, java.util.List<Integer> payStatuses, Long storeId,
                           Integer pageNum, Integer pageSize, HttpServletRequest httpRequest);

    Response mockConfirmPay(String orderNo, HttpServletRequest httpRequest);

    String handleWxPayNotify(HttpServletRequest request, String body);

    /** 待支付且微信已付：补记支付（含扣库存） */
    void applyPaidIfUnpaid(String orderNo, String transactionId);

    /** 待支付关单，仅 pay_status=0 时生效 */
    void closeIfUnpaid(String orderNo);

    /** 商户/管理员发起退款（全额或部分） */
    Response refund(MallRefundRequest request, HttpServletRequest httpRequest);

    /** 微信退款结果回调 */
    String handleWxRefundNotify(HttpServletRequest request, String body);

    /**
     * 店铺订单金额流水。商家查本店；管理员必须先传 storeId。
     * periodType: year / quarter / month / day
     */
    Response incomeFlow(Long storeId, String periodType, Integer year, Integer yearFrom, Integer yearTo,
                        Integer month, HttpServletRequest httpRequest);

    /**
     * 店铺资金明细：每笔已支付订单记收入，有退款再记一笔退款。
     * 商家查本店；管理员须传 storeId。type: all / income / expense
     */
    Response financeLedger(Long storeId, String type, String date, Integer pageNum, Integer pageSize,
                           HttpServletRequest httpRequest);

    Response previewCheckout(MallCheckoutRequest request, HttpServletRequest httpRequest);
}
