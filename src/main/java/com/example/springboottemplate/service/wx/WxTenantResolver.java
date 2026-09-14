package com.example.springboottemplate.service.wx;

import com.example.springboottemplate.config.WxPayProperties;
import com.example.springboottemplate.config.WxProperties;
import com.example.springboottemplate.entity.MallOrder;
import com.example.springboottemplate.entity.StoreWxConfig;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.StoreWxConfigMapper;
import com.example.springboottemplate.utils.SecretCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 按 appId / storeId / 订单解析微信租户。无店铺配置时回落到 yml 平台默认（试用代收）。
 */
@Component
public class WxTenantResolver {

    @Autowired
    private WxProperties wxProperties;
    @Autowired
    private WxPayProperties wxPayProperties;
    @Autowired
    private StoreWxConfigMapper storeWxConfigMapper;
    @Autowired
    private SecretCodec secretCodec;

    public WxTenantContext platform() {
        return WxTenantContext.builder()
                .source(WxTenantContext.SOURCE_PLATFORM)
                .appId(trimToNull(wxProperties.getAppId()))
                .appSecret(trimToNull(wxProperties.getAppSecret()))
                .mchId(trimToNull(wxPayProperties.getMchId()))
                .apiV3Key(trimToNull(wxPayProperties.getApiV3Key()))
                .merchantSerialNumber(trimToNull(wxPayProperties.getMerchantSerialNumber()))
                .privateKey(resolvePem(wxPayProperties.getPrivateKey(), wxPayProperties.getPrivateKeyPath()))
                .publicKey(resolvePem(wxPayProperties.getPublicKey(), wxPayProperties.getPublicKeyPath()))
                .publicKeyId(trimToNull(wxPayProperties.getPublicKeyId()))
                .notifyUrl(trimToNull(wxPayProperties.getNotifyUrl()))
                .refundNotifyUrl(trimToNull(wxPayProperties.getRefundNotifyUrl()))
                .build();
    }

    public boolean isPlatformAppId(String appId) {
        if (!StringUtils.hasText(appId)) {
            return true;
        }
        String platformAppId = wxProperties.getAppId();
        return StringUtils.hasText(platformAppId) && platformAppId.equals(appId.trim());
    }

    /**
     * 登录 / 授权手机号：空或平台 appId 走 yml；其它必须已配置且启用。
     */
    public WxTenantContext byAppId(String appId) {
        if (isPlatformAppId(appId)) {
            WxTenantContext platform = platform();
            if (!platform.miniappReady()) {
                throw new BusinessException("未配置微信小程序 appId/appSecret");
            }
            return platform;
        }
        StoreWxConfig cfg = storeWxConfigMapper.selectByAppId(appId.trim());
        if (cfg == null) {
            throw new BusinessException("未识别的小程序，请联系管理员配置");
        }
        WxTenantContext ctx = fromConfig(cfg);
        if (!ctx.miniappReady()) {
            throw new BusinessException("该小程序尚未配置 AppSecret");
        }
        return ctx;
    }

    /**
     * 下单 / 退款：无配置 = 试用代收到平台；有配置但支付材料不齐则明确失败。
     */
    public WxTenantContext byStoreId(Long storeId) {
        if (storeId == null) {
            return platform();
        }
        StoreWxConfig cfg = storeWxConfigMapper.selectByStoreId(storeId);
        if (cfg == null || cfg.getEnabled() == null || cfg.getEnabled() != 1) {
            return platform();
        }
        WxTenantContext ctx = fromConfig(cfg);
        if (!ctx.payReady()) {
            throw new BusinessException("该店尚未开通微信支付，请联系管理员配置商户号");
        }
        return ctx;
    }

    /** 回调按 URL 中的 storeId 选证书；无配置则平台。不因材料不齐抛业务异常。 */
    public WxTenantContext byStoreIdForNotify(Long storeId) {
        if (storeId == null) {
            return platform();
        }
        StoreWxConfig cfg = storeWxConfigMapper.selectByStoreId(storeId);
        if (cfg == null || cfg.getEnabled() == null || cfg.getEnabled() != 1) {
            return platform();
        }
        return fromConfig(cfg);
    }

    public WxTenantContext byMchId(String mchId) {
        if (!StringUtils.hasText(mchId)) {
            return platform();
        }
        String platformMch = wxPayProperties.getMchId();
        if (StringUtils.hasText(platformMch) && platformMch.equals(mchId.trim())) {
            return platform();
        }
        StoreWxConfig cfg = storeWxConfigMapper.selectByMchId(mchId.trim());
        if (cfg == null) {
            return platform();
        }
        return fromConfig(cfg);
    }

    /**
     * 关单 / 发货 / 退款：订单已记 mchId 用它；老订单无字段走平台，避免买断后误查新商户号。
     */
    public WxTenantContext byOrder(MallOrder order) {
        if (order == null) {
            return platform();
        }
        if (StringUtils.hasText(order.getMchId())) {
            return byMchId(order.getMchId());
        }
        if (!StringUtils.hasText(order.getAppId()) && !StringUtils.hasText(order.getMchId())) {
            return platform();
        }
        if (isPlatformAppId(order.getAppId())) {
            return platform();
        }
        return byStoreId(order.getStoreId());
    }

    public WxTenantContext fromConfig(StoreWxConfig cfg) {
        Long storeId = cfg.getStoreId();
        return WxTenantContext.builder()
                .storeId(storeId)
                .source(WxTenantContext.SOURCE_STORE)
                .appId(trimToNull(cfg.getAppId()))
                .appSecret(secretCodec.decrypt(trimToNull(cfg.getAppSecret())))
                .mchId(trimToNull(cfg.getMchId()))
                .apiV3Key(secretCodec.decrypt(trimToNull(cfg.getApiV3Key())))
                .merchantSerialNumber(trimToNull(cfg.getMerchantSerialNumber()))
                .privateKey(secretCodec.decrypt(trimToNull(cfg.getPrivateKey())))
                .publicKey(secretCodec.decrypt(trimToNull(cfg.getPublicKey())))
                .publicKeyId(trimToNull(cfg.getPublicKeyId()))
                .notifyUrl(firstText(trimToNull(cfg.getNotifyUrl()), defaultNotifyUrl(storeId)))
                .refundNotifyUrl(firstText(trimToNull(cfg.getRefundNotifyUrl()), defaultRefundNotifyUrl(storeId)))
                .build();
    }

    public String defaultNotifyUrl(Long storeId) {
        return appendStoreId(wxPayProperties.getNotifyUrl(), storeId);
    }

    public String defaultRefundNotifyUrl(Long storeId) {
        return appendStoreId(wxPayProperties.getRefundNotifyUrl(), storeId);
    }

    private String appendStoreId(String base, Long storeId) {
        if (!StringUtils.hasText(base) || storeId == null) {
            return trimToNull(base);
        }
        String url = base.trim();
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url + "/" + storeId;
    }

    private static String firstText(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }

    private static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private static String resolvePem(String inline, String path) {
        if (StringUtils.hasText(inline)) {
            return inline.trim();
        }
        if (!StringUtils.hasText(path)) {
            return null;
        }
        try {
            return Files.readString(Path.of(path.trim()));
        } catch (Exception e) {
            return null;
        }
    }
}
