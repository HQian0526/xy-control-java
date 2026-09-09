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
    /** 退款结果回调 URL（公网 HTTPS） */
    private String refundNotifyUrl;
    /** 配送费（元） */
    private BigDecimal deliveryFee = BigDecimal.ZERO;
    /** 支付成功后向微信发货信息管理报发货 */
    private Shipping shipping = new Shipping();
    /** 待支付超时关单 */
    private Unpaid unpaid = new Unpaid();

    @Data
    public static class Unpaid {
        /** 未支付超时分钟数，同时写入微信 time_expire */
        private int timeoutMinutes = 15;
        /** 是否启用定时扫尾 */
        private boolean scanEnabled = true;
        /** 定时扫描间隔（毫秒），上一轮结束后再等该时间 */
        private long scanIntervalMs = 120000L;
        /** 每次扫描最多处理多少笔 */
        private int scanBatchSize = 50;

        public int resolvedTimeoutMinutes() {
            return Math.max(2, Math.min(timeoutMinutes, 7 * 24 * 60));
        }

        public int resolvedScanBatchSize() {
            return Math.max(1, scanBatchSize);
        }
    }

    @Data
    public static class Shipping {
        /** 是否在支付成功后自动报发货 */
        private boolean enabled = true;
        /**
         * 物流模式：2同城配送 4用户自提。
         * 社区实物不要填 3（虚拟/无需物流），也不要填 1（快递，需运单号）。
         */
        private int logisticsType = 2;
        /** 失败最大重试次数 */
        private int maxRetry = 8;
        /** 定时扫描未同步订单的间隔（毫秒） */
        private long retryIntervalMs = 120000L;
        /** 每次扫描最多处理多少笔 */
        private int retryBatchSize = 20;
    }
}
