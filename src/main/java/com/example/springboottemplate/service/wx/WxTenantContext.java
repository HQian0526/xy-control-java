package com.example.springboottemplate.service.wx;

import lombok.Builder;
import lombok.Value;
import org.springframework.util.StringUtils;

/**
 * 解析后的租户微信上下文：登录用 appId/secret，支付用商户证书。
 */
@Value
@Builder
public class WxTenantContext {
    public static final String SOURCE_PLATFORM = "platform";
    public static final String SOURCE_STORE = "store";

    Long storeId;
    String source;
    String appId;
    String appSecret;
    String mchId;
    String apiV3Key;
    String merchantSerialNumber;
    String privateKey;
    String publicKey;
    String publicKeyId;
    String notifyUrl;
    String refundNotifyUrl;

    public boolean isPlatform() {
        return SOURCE_PLATFORM.equals(source);
    }

    public boolean miniappReady() {
        return StringUtils.hasText(appId) && StringUtils.hasText(appSecret);
    }

    public boolean payReady() {
        return StringUtils.hasText(mchId)
                && StringUtils.hasText(apiV3Key)
                && StringUtils.hasText(merchantSerialNumber)
                && StringUtils.hasText(privateKey)
                && StringUtils.hasText(publicKey)
                && StringUtils.hasText(publicKeyId)
                && StringUtils.hasText(notifyUrl);
    }
}
