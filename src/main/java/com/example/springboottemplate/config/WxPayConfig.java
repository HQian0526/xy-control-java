package com.example.springboottemplate.config;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.refund.RefundService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 微信支付配置（公钥模式）。
 * 新商户已无平台证书，需使用商户平台「API安全」中的微信支付公钥，
 * 不能再用会请求 /v3/certificates 的 RSAAutoCertificateConfig。
 */
@Configuration
@ConditionalOnProperty(prefix = "wechat.pay", name = "mock", havingValue = "false")
public class WxPayConfig {

    @Bean
    public Config wxPayRsaConfig(WxPayProperties payProperties) {
        ensureConfigured(payProperties);
        RSAPublicKeyConfig.Builder builder = new RSAPublicKeyConfig.Builder()
                .merchantId(payProperties.getMchId())
                .merchantSerialNumber(payProperties.getMerchantSerialNumber())
                .apiV3Key(payProperties.getApiV3Key())
                .publicKeyId(payProperties.getPublicKeyId());

        if (StringUtils.hasText(payProperties.getPrivateKey())) {
            builder.privateKey(payProperties.getPrivateKey());
        } else {
            builder.privateKeyFromPath(payProperties.getPrivateKeyPath());
        }

        if (StringUtils.hasText(payProperties.getPublicKey())) {
            builder.publicKey(payProperties.getPublicKey());
        } else {
            builder.publicKeyFromPath(payProperties.getPublicKeyPath());
        }

        return builder.build();
    }

    @Bean
    public JsapiService jsapiService(Config wxPayRsaConfig) {
        return new JsapiService.Builder().config(wxPayRsaConfig).build();
    }

    @Bean
    public JsapiServiceExtension jsapiServiceExtension(Config wxPayRsaConfig) {
        return new JsapiServiceExtension.Builder().config(wxPayRsaConfig).build();
    }

    @Bean
    public RefundService refundService(Config wxPayRsaConfig) {
        return new RefundService.Builder().config(wxPayRsaConfig).build();
    }

    @Bean
    public NotificationParser notificationParser(Config wxPayRsaConfig) {
        return new NotificationParser((NotificationConfig) wxPayRsaConfig);
    }

    private void ensureConfigured(WxPayProperties payProperties) {
        if (!StringUtils.hasText(payProperties.getMchId())
                || !StringUtils.hasText(payProperties.getApiV3Key())
                || !StringUtils.hasText(payProperties.getMerchantSerialNumber())
                || (!StringUtils.hasText(payProperties.getPrivateKey())
                && !StringUtils.hasText(payProperties.getPrivateKeyPath()))
                || (!StringUtils.hasText(payProperties.getPublicKey())
                && !StringUtils.hasText(payProperties.getPublicKeyPath()))
                || !StringUtils.hasText(payProperties.getPublicKeyId())
                || !StringUtils.hasText(payProperties.getNotifyUrl())) {
            throw new IllegalStateException(
                    "wechat.pay.mock=false 时必须配置：mch-id、api-v3-key、merchant-serial-number、"
                            + "private-key(或path)、微信支付公钥 public-key(或path)、public-key-id、notify-url。"
                            + "请到商户平台-API安全申请/下载微信支付公钥，见 https://pay.weixin.qq.com/doc/v3/merchant/4012153196");
        }
    }
}
