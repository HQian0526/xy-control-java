package com.example.springboottemplate.service.wx;

import com.example.springboottemplate.config.WxPayProperties;
import com.example.springboottemplate.config.WxProperties;
import com.example.springboottemplate.dto.mall.MallPayPrepareVO;
import com.example.springboottemplate.exception.BusinessException;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * 微信支付封装：真实下单 / mock 分流
 */
@Service
public class WxPayClientService {

    @Autowired
    private WxPayProperties wxPayProperties;
    @Autowired
    private WxProperties wxProperties;
    @Autowired(required = false)
    private JsapiServiceExtension jsapiServiceExtension;
    @Autowired(required = false)
    private JsapiService jsapiService;
    @Autowired(required = false)
    private NotificationParser notificationParser;

    public boolean isMock() {
        return wxPayProperties.isMock() || !wxPayProperties.isEnabled();
    }

    public MallPayPrepareVO createJsapiPrepay(String orderNo, String description, String openid, Integer totalFen) {
        if (isMock()) {
            return MallPayPrepareVO.builder()
                    .mock(true)
                    .orderNo(orderNo)
                    .payAmount(fenToYuan(totalFen))
                    .payStatus(0)
                    .appId(wxProperties.getAppId())
                    .build();
        }
        if (jsapiServiceExtension == null) {
            throw new BusinessException("微信支付未正确初始化，请检查商户配置");
        }
        if (!StringUtils.hasText(openid)) {
            throw new BusinessException("当前账号未绑定微信，无法支付");
        }

        PrepayRequest request = new PrepayRequest();
        request.setAppid(wxProperties.getAppId());
        request.setMchid(wxPayProperties.getMchId());
        request.setDescription(truncate(description, 127));
        request.setOutTradeNo(orderNo);
        request.setNotifyUrl(wxPayProperties.getNotifyUrl());

        Amount amount = new Amount();
        amount.setTotal(totalFen);
        amount.setCurrency("CNY");
        request.setAmount(amount);

        Payer payer = new Payer();
        payer.setOpenid(openid);
        request.setPayer(payer);

        try {
            PrepayWithRequestPaymentResponse resp = jsapiServiceExtension.prepayWithRequestPayment(request);
            return MallPayPrepareVO.builder()
                    .mock(false)
                    .orderNo(orderNo)
                    .payAmount(fenToYuan(totalFen))
                    .payStatus(0)
                    .appId(resp.getAppId())
                    .timeStamp(resp.getTimeStamp())
                    .nonceStr(resp.getNonceStr())
                    .packageValue(resp.getPackageVal())
                    .signType(resp.getSignType())
                    .paySign(resp.getPaySign())
                    .build();
        } catch (ServiceException e) {
            throw new BusinessException("微信下单失败: " + e.getErrorMessage());
        } catch (Exception e) {
            throw new BusinessException("微信下单失败: " + e.getMessage(), e);
        }
    }

    public Transaction parseNotify(String body, String serial, String nonce, String signature, String timestamp, String signType) {
        if (isMock() || notificationParser == null) {
            throw new BusinessException("当前为 mock 模式，不处理微信回调");
        }
        RequestParam.Builder builder = new RequestParam.Builder()
                .serialNumber(serial)
                .nonce(nonce)
                .signature(signature)
                .timestamp(timestamp)
                .body(body);
        if (StringUtils.hasText(signType)) {
            builder.signType(signType);
        }
        return notificationParser.parse(builder.build(), Transaction.class);
    }

    public Transaction queryByOutTradeNo(String orderNo) {
        if (isMock() || jsapiService == null) {
            return null;
        }
        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        request.setMchid(wxPayProperties.getMchId());
        request.setOutTradeNo(orderNo);
        try {
            return jsapiService.queryOrderByOutTradeNo(request);
        } catch (ServiceException e) {
            throw new BusinessException("查询微信支付单失败: " + e.getErrorMessage());
        }
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return "商城订单";
        }
        return text.length() <= max ? text : text.substring(0, max);
    }

    private static BigDecimal fenToYuan(Integer fen) {
        if (fen == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(fen).movePointLeft(2);
    }
}
