package com.example.springboottemplate.service.wx;

import com.example.springboottemplate.config.WxPayProperties;
import com.example.springboottemplate.dto.mall.MallPayPrepareVO;
import com.example.springboottemplate.exception.BusinessException;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import com.wechat.pay.java.service.refund.model.ReqFundsAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 微信支付封装：真实下单 / mock 分流
 */
@Service
public class WxPayClientService {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter RFC3339 =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    @Autowired
    private WxPayProperties wxPayProperties;
    @Autowired
    private WxPayClientFactory wxPayClientFactory;
    @Autowired
    private WxTenantResolver wxTenantResolver;

    public boolean isMock() {
        return wxPayProperties.isMock() || !wxPayProperties.isEnabled();
    }

    public MallPayPrepareVO createJsapiPrepay(String orderNo, String description, String openid, Integer totalFen) {
        return createJsapiPrepay(wxTenantResolver.platform(), orderNo, description, openid, totalFen);
    }

    public MallPayPrepareVO createJsapiPrepay(WxTenantContext tenant, String orderNo, String description,
                                              String openid, Integer totalFen) {
        if (tenant == null) {
            tenant = wxTenantResolver.platform();
        }
        if (isMock()) {
            return MallPayPrepareVO.builder()
                    .mock(true)
                    .orderNo(orderNo)
                    .payAmount(fenToYuan(totalFen))
                    .payStatus(0)
                    .appId(tenant.getAppId())
                    .build();
        }
        WxPayClientFactory.Clients clients = wxPayClientFactory.clients(tenant);
        if (clients.jsapiServiceExtension() == null) {
            throw new BusinessException("微信支付未正确初始化，请检查商户配置");
        }
        if (!StringUtils.hasText(openid)) {
            throw new BusinessException("当前账号未绑定微信，无法支付");
        }

        PrepayRequest request = new PrepayRequest();
        request.setAppid(tenant.getAppId());
        request.setMchid(tenant.getMchId());
        request.setDescription(truncate(description, 127));
        request.setOutTradeNo(orderNo);
        request.setNotifyUrl(tenant.getNotifyUrl());
        request.setTimeExpire(buildTimeExpire());

        Amount amount = new Amount();
        amount.setTotal(totalFen);
        amount.setCurrency("CNY");
        request.setAmount(amount);

        Payer payer = new Payer();
        payer.setOpenid(openid);
        request.setPayer(payer);

        try {
            PrepayWithRequestPaymentResponse resp = clients.jsapiServiceExtension().prepayWithRequestPayment(request);
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
        return parseNotify(wxTenantResolver.platform(), body, serial, nonce, signature, timestamp, signType);
    }

    public Transaction parseNotify(WxTenantContext tenant, String body, String serial, String nonce,
                                   String signature, String timestamp, String signType) {
        NotificationParser parser = resolveParser(tenant);
        if (isMock() || parser == null) {
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
        return parser.parse(builder.build(), Transaction.class);
    }

    public Transaction queryByOutTradeNo(String orderNo) {
        return queryByOutTradeNo(wxTenantResolver.platform(), orderNo);
    }

    public Transaction queryByOutTradeNo(WxTenantContext tenant, String orderNo) {
        if (isMock()) {
            return null;
        }
        WxPayClientFactory.Clients clients = wxPayClientFactory.clients(tenant);
        if (clients.jsapiService() == null) {
            return null;
        }
        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        request.setMchid(tenant.getMchId());
        request.setOutTradeNo(orderNo);
        try {
            return clients.jsapiService().queryOrderByOutTradeNo(request);
        } catch (ServiceException e) {
            throw new BusinessException("查询微信支付单失败: " + e.getErrorCode() + " " + e.getErrorMessage());
        }
    }

    private String buildTimeExpire() {
        int minutes = wxPayProperties.getUnpaid() == null
                ? 15
                : wxPayProperties.getUnpaid().resolvedTimeoutMinutes();
        return OffsetDateTime.now(SHANGHAI).plusMinutes(minutes).format(RFC3339);
    }

    /**
     * 申请退款。
     *
     * @param outTradeNo   商户订单号
     * @param outRefundNo  商户退款单号
     * @param totalFen     原订单金额（分）
     * @param refundFen    本次退款金额（分）
     * @param reason       退款原因
     */
    public Refund createRefund(String outTradeNo, String outRefundNo, int totalFen, int refundFen, String reason) {
        return createRefund(wxTenantResolver.platform(), outTradeNo, outRefundNo, totalFen, refundFen, reason);
    }

    public Refund createRefund(WxTenantContext tenant, String outTradeNo, String outRefundNo,
                               int totalFen, int refundFen, String reason) {
        if (tenant == null) {
            tenant = wxTenantResolver.platform();
        }
        if (isMock()) {
            Refund mock = new Refund();
            mock.setOutTradeNo(outTradeNo);
            mock.setOutRefundNo(outRefundNo);
            mock.setRefundId("MOCKRF" + outRefundNo);
            mock.setStatus(com.wechat.pay.java.service.refund.model.Status.SUCCESS);
            com.wechat.pay.java.service.refund.model.Amount amount =
                    new com.wechat.pay.java.service.refund.model.Amount();
            amount.setTotal((long) totalFen);
            amount.setRefund((long) refundFen);
            amount.setPayerRefund((long) refundFen);
            mock.setAmount(amount);
            return mock;
        }
        WxPayClientFactory.Clients clients = wxPayClientFactory.clients(tenant);
        if (clients.refundService() == null) {
            throw new BusinessException("微信支付退款未正确初始化，请检查商户配置");
        }
        if (!StringUtils.hasText(tenant.getRefundNotifyUrl())) {
            throw new BusinessException("未配置退款回调地址");
        }

        CreateRequest request = new CreateRequest();
        request.setOutTradeNo(outTradeNo);
        request.setOutRefundNo(outRefundNo);
        request.setNotifyUrl(tenant.getRefundNotifyUrl());
        if (StringUtils.hasText(reason)) {
            request.setReason(truncate(reason, 80));
        }
        request.setFundsAccount(ReqFundsAccount.AVAILABLE);

        AmountReq amount = new AmountReq();
        amount.setRefund((long) refundFen);
        amount.setTotal((long) totalFen);
        amount.setCurrency("CNY");
        request.setAmount(amount);

        try {
            return clients.refundService().create(request);
        } catch (ServiceException e) {
            throw new BusinessException("微信退款失败: " + e.getErrorMessage());
        } catch (Exception e) {
            throw new BusinessException("微信退款失败: " + e.getMessage(), e);
        }
    }

    public RefundNotification parseRefundNotify(String body, String serial, String nonce,
                                                String signature, String timestamp, String signType) {
        return parseRefundNotify(wxTenantResolver.platform(), body, serial, nonce, signature, timestamp, signType);
    }

    public RefundNotification parseRefundNotify(WxTenantContext tenant, String body, String serial, String nonce,
                                                String signature, String timestamp, String signType) {
        NotificationParser parser = resolveParser(tenant);
        if (isMock() || parser == null) {
            throw new BusinessException("当前为 mock 模式，不处理微信退款回调");
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
        return parser.parse(builder.build(), RefundNotification.class);
    }

    private NotificationParser resolveParser(WxTenantContext tenant) {
        if (tenant == null) {
            tenant = wxTenantResolver.platform();
        }
        return wxPayClientFactory.clients(tenant).notificationParser();
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
