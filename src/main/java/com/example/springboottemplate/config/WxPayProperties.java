package com.example.springboottemplate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "wechat.pay")
public class WxPayProperties {
    /** 是否启用微信支付能力 */
    private boolean enabled = true;
    /** true=本地 mock，不调微信，便于先打通商城结算业务流 */
    private boolean mock = true;
    private String mchId;
    private String apiV3Key;
    private String merchantSerialNumber;
    /** PEM 私钥全文 */
    private String privateKey;
    /** 私钥文件路径（与 privateKey 二选一） */
    private String privateKeyPath;
    /**
     * 微信支付公钥 PEM 全文（商户平台-API安全下载的 pub_key.pem，不是商户私钥）
     */
    private String publicKey;
    /** 微信支付公钥文件路径（与 publicKey 二选一） */
    private String publicKeyPath;
    /** 微信支付公钥 ID，形如 PUB_KEY_ID_xxx */
    private String publicKeyId;
    private String notifyUrl;
    /** 配送费（元） */
    private BigDecimal deliveryFee = BigDecimal.ZERO;
}
