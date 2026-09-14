package com.example.springboottemplate.service.wx;

import com.example.springboottemplate.exception.BusinessException;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.refund.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 按商户号缓存微信支付 Client。yml mock=true 时不会真正构建。
 */
@Component
public class WxPayClientFactory {

    @Autowired(required = false)
    private JsapiService platformJsapiService;
    @Autowired(required = false)
    private JsapiServiceExtension platformJsapiServiceExtension;
    @Autowired(required = false)
    private RefundService platformRefundService;
    @Autowired(required = false)
    private NotificationParser platformNotificationParser;

    private final ConcurrentHashMap<String, Clients> cache = new ConcurrentHashMap<>();

    public Clients clients(WxTenantContext tenant) {
        if (tenant == null) {
            throw new BusinessException("微信支付租户未解析");
        }
        if (tenant.isPlatform()) {
            return new Clients(
                    platformJsapiService,
                    platformJsapiServiceExtension,
                    platformRefundService,
                    platformNotificationParser
            );
        }
        String mchId = tenant.getMchId();
        if (!StringUtils.hasText(mchId)) {
            throw new BusinessException("该店尚未开通微信支付");
        }
        return cache.computeIfAbsent(mchId, key -> build(tenant));
    }

    public void evict(String mchId) {
        if (StringUtils.hasText(mchId)) {
            cache.remove(mchId.trim());
        }
    }

    private Clients build(WxTenantContext tenant) {
        if (!tenant.payReady()) {
            throw new BusinessException("该店尚未开通微信支付，请联系管理员配置商户号");
        }
        try {
            Config config = new RSAPublicKeyConfig.Builder()
                    .merchantId(tenant.getMchId())
                    .merchantSerialNumber(tenant.getMerchantSerialNumber())
                    .apiV3Key(tenant.getApiV3Key())
                    .publicKeyId(tenant.getPublicKeyId())
                    .privateKey(tenant.getPrivateKey())
                    .publicKey(tenant.getPublicKey())
                    .build();
            return new Clients(
                    new JsapiService.Builder().config(config).build(),
                    new JsapiServiceExtension.Builder().config(config).build(),
                    new RefundService.Builder().config(config).build(),
                    new NotificationParser((NotificationConfig) config)
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("初始化店铺微信支付失败: " + e.getMessage(), e);
        }
    }

    public static class Clients {
        private final JsapiService jsapiService;
        private final JsapiServiceExtension jsapiServiceExtension;
        private final RefundService refundService;
        private final NotificationParser notificationParser;

        public Clients(JsapiService jsapiService,
                       JsapiServiceExtension jsapiServiceExtension,
                       RefundService refundService,
                       NotificationParser notificationParser) {
            this.jsapiService = jsapiService;
            this.jsapiServiceExtension = jsapiServiceExtension;
            this.refundService = refundService;
            this.notificationParser = notificationParser;
        }

        public JsapiService jsapiService() {
            return jsapiService;
        }

        public JsapiServiceExtension jsapiServiceExtension() {
            return jsapiServiceExtension;
        }

        public RefundService refundService() {
            return refundService;
        }

        public NotificationParser notificationParser() {
            return notificationParser;
        }
    }
}
