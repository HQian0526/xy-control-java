package com.example.springboottemplate.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.StoreMiniappUiSaveRequest;
import com.example.springboottemplate.dto.StoreWxConfigSaveRequest;
import com.example.springboottemplate.dto.StoreWxConfigVO;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.StoreWxConfig;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.mapper.StoreWxConfigMapper;
import com.example.springboottemplate.service.StoreWxConfigService;
import com.example.springboottemplate.service.wx.WxPayClientFactory;
import com.example.springboottemplate.service.wx.WxTenantContext;
import com.example.springboottemplate.service.wx.WxTenantResolver;
import com.example.springboottemplate.utils.MiniappUiHelper;
import com.example.springboottemplate.utils.SecretCodec;
import com.example.springboottemplate.utils.StoreAuthHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StoreWxConfigServiceimpl implements StoreWxConfigService {

    @Autowired
    private StoreWxConfigMapper storeWxConfigMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private StoreAuthHelper storeAuthHelper;
    @Autowired
    private SecretCodec secretCodec;
    @Autowired
    private WxTenantResolver wxTenantResolver;
    @Autowired
    private WxPayClientFactory wxPayClientFactory;

    @Override
    public Response getByStoreId(Long storeId, HttpServletRequest request) {
        storeAuthHelper.requireAdmin(request);
        if (storeId == null) {
            throw new BusinessException("请选择店铺");
        }
        Store store = storeMapper.selectByStoreId(storeId);
        if (store == null) {
            throw new BusinessException("店铺不存在");
        }
        StoreWxConfig cfg = storeWxConfigMapper.selectByStoreId(storeId);
        return Response.success(toVo(store, cfg));
    }

    @Override
    public Response list(HttpServletRequest request) {
        storeAuthHelper.requireAdmin(request);
        Store query = new Store();
        query.setDeleted(0);
        @SuppressWarnings("unchecked")
        List<Store> stores = storeMapper.findStore(query);
        List<StoreWxConfig> configs = storeWxConfigMapper.findStoreWxConfig(new StoreWxConfig());
        Map<Long, StoreWxConfig> byStore = new HashMap<>();
        if (configs != null) {
            for (StoreWxConfig cfg : configs) {
                if (cfg != null && cfg.getStoreId() != null) {
                    byStore.put(cfg.getStoreId(), cfg);
                }
            }
        }
        List<StoreWxConfigVO> list = new ArrayList<>();
        if (stores != null) {
            for (Store store : stores) {
                if (store == null || store.getStoreId() == null) {
                    continue;
                }
                list.add(toVo(store, byStore.get(store.getStoreId())));
            }
        }
        return Response.success(list);
    }

    @Override
    public Response save(StoreWxConfigSaveRequest body, HttpServletRequest request) {
        StoreAuthHelper.Operator operator = storeAuthHelper.requireAdmin(request);
        if (body == null || body.getStoreId() == null) {
            throw new BusinessException("请选择店铺");
        }
        Store store = storeMapper.selectByStoreId(body.getStoreId());
        if (store == null) {
            throw new BusinessException("店铺不存在");
        }

        String appId = trimToNull(body.getAppId());
        String mchId = trimToNull(body.getMchId());

        StoreWxConfig existing = storeWxConfigMapper.selectByStoreId(body.getStoreId());
        String oldMchId = existing == null ? null : existing.getMchId();
        Date now = new Date();
        try {
            if (existing == null) {
                StoreWxConfig record = StoreWxConfig.builder()
                        .id(IdWorker.getId())
                        .storeId(body.getStoreId())
                        .appId(appId)
                        .appSecret(encryptIfPresent(body.getAppSecret()))
                        .mchId(mchId)
                        .apiV3Key(encryptIfPresent(body.getApiV3Key()))
                        .merchantSerialNumber(trimToNull(body.getMerchantSerialNumber()))
                        .privateKey(encryptIfPresent(body.getPrivateKey()))
                        .publicKey(encryptIfPresent(body.getPublicKey()))
                        .publicKeyId(trimToNull(body.getPublicKeyId()))
                        .notifyUrl(trimToEmptyAllowed(body.getNotifyUrl()))
                        .refundNotifyUrl(trimToEmptyAllowed(body.getRefundNotifyUrl()))
                        .enabled(body.getEnabled() == null ? 1 : body.getEnabled())
                        .createdTime(now)
                        .createdBy(operator.getUsername())
                        .deleted(0)
                        .build();
                storeWxConfigMapper.addStoreWxConfig(record);
                existing = record;
            } else {
                existing.setAppId(appId);
                existing.setAppSecret(null);
                existing.setApiV3Key(null);
                existing.setPrivateKey(null);
                existing.setPublicKey(null);
                if (hasSecretInput(body.getAppSecret())) {
                    existing.setAppSecret(secretCodec.encrypt(body.getAppSecret().trim()));
                }
                existing.setMchId(mchId);
                if (hasSecretInput(body.getApiV3Key())) {
                    existing.setApiV3Key(secretCodec.encrypt(body.getApiV3Key().trim()));
                }
                existing.setMerchantSerialNumber(trimToNull(body.getMerchantSerialNumber()));
                if (hasSecretInput(body.getPrivateKey())) {
                    existing.setPrivateKey(secretCodec.encrypt(body.getPrivateKey().trim()));
                }
                if (hasSecretInput(body.getPublicKey())) {
                    existing.setPublicKey(secretCodec.encrypt(body.getPublicKey().trim()));
                }
                existing.setPublicKeyId(trimToNull(body.getPublicKeyId()));
                existing.setNotifyUrl(trimToEmptyAllowed(body.getNotifyUrl()));
                existing.setRefundNotifyUrl(trimToEmptyAllowed(body.getRefundNotifyUrl()));
                existing.setEnabled(body.getEnabled() == null ? existing.getEnabled() : body.getEnabled());
                existing.setUpdateBy(operator.getUsername());
                storeWxConfigMapper.updateStoreWxConfig(existing);
            }
        } catch (DuplicateKeyException e) {
            throw new BusinessException("该店铺已配置过微信支付");
        }

        wxPayClientFactory.evict(oldMchId);
        wxPayClientFactory.evict(mchId);
        StoreWxConfig saved = storeWxConfigMapper.selectByStoreId(body.getStoreId());
        return Response.success(toVo(store, saved));
    }

    @Override
    public Response saveMiniappUi(StoreMiniappUiSaveRequest body, HttpServletRequest request) {
        StoreAuthHelper.Operator operator = storeAuthHelper.requireAdmin(request);
        if (body == null || body.getStoreId() == null) {
            throw new BusinessException("请选择店铺");
        }
        if (body.getMiniappType() == null) {
            throw new BusinessException("请选择小程序类型");
        }
        Store store = storeMapper.selectByStoreId(body.getStoreId());
        if (store == null || store.getId() == null) {
            throw new BusinessException("店铺不存在");
        }
        Store patch = new Store();
        patch.setId(store.getId());
        MiniappUiHelper.applySave(
                patch,
                body.getMiniappType(),
                body.getShowStock(),
                body.getShowAddress(),
                body.getShowQuantity(),
                body.getShowCart(),
                body.getOrderBtnText(),
                body.getEnablePay(),
                body.getHomeTabText(),
                body.getListBtnText(),
                body.getShowSaleNum(),
                body.getShowDeliveryFee(),
                body.getDeliveryFee(),
                body.getEnableRecharge());
        patch.setUpdateBy(operator.getUsername());
        storeMapper.updateStore(patch);
        Store refreshed = storeMapper.selectByStoreId(body.getStoreId());
        StoreWxConfig cfg = storeWxConfigMapper.selectByStoreId(body.getStoreId());
        return Response.success(toVo(refreshed != null ? refreshed : store, cfg));
    }

    private StoreWxConfigVO toVo(Store store, StoreWxConfig cfg) {
        StoreWxConfigVO vo = new StoreWxConfigVO();
        vo.setStoreId(store.getStoreId());
        vo.setStoreName(store.getStoreName());
        MiniappUiHelper.fillDefaults(store);
        vo.setMiniappType(store.getMiniappType());
        vo.setShowStock(store.getShowStock());
        vo.setShowAddress(store.getShowAddress());
        vo.setShowQuantity(store.getShowQuantity());
        vo.setShowCart(store.getShowCart());
        vo.setOrderBtnText(store.getOrderBtnText());
        vo.setEnablePay(store.getEnablePay());
        vo.setEnableRecharge(store.getEnableRecharge());
        vo.setHomeTabText(store.getHomeTabText());
        vo.setListBtnText(store.getListBtnText());
        vo.setShowSaleNum(store.getShowSaleNum());
        vo.setShowDeliveryFee(store.getShowDeliveryFee());
        vo.setDeliveryFee(store.getDeliveryFee());
        if (cfg == null) {
            vo.setEnabled(0);
            vo.setPlatformFallback(true);
            vo.setMiniappReady(false);
            vo.setPayReady(false);
            return vo;
        }
        vo.setAppId(cfg.getAppId());
        vo.setMchId(cfg.getMchId());
        vo.setMerchantSerialNumber(cfg.getMerchantSerialNumber());
        vo.setPublicKeyId(cfg.getPublicKeyId());
        vo.setNotifyUrl(cfg.getNotifyUrl());
        vo.setRefundNotifyUrl(cfg.getRefundNotifyUrl());
        vo.setEnabled(cfg.getEnabled());
        vo.setHasAppSecret(StringUtils.hasText(cfg.getAppSecret()));
        vo.setHasApiV3Key(StringUtils.hasText(cfg.getApiV3Key()));
        vo.setHasPrivateKey(StringUtils.hasText(cfg.getPrivateKey()));
        vo.setHasPublicKey(StringUtils.hasText(cfg.getPublicKey()));
        vo.setPlatformFallback(cfg.getEnabled() == null || cfg.getEnabled() != 1);
        WxTenantContext ctx = wxTenantResolver.fromConfig(cfg);
        vo.setMiniappReady(ctx.miniappReady());
        vo.setPayReady(ctx.payReady());
        return vo;
    }

    private String encryptIfPresent(String raw) {
        return hasSecretInput(raw) ? secretCodec.encrypt(raw.trim()) : null;
    }

    private static boolean hasSecretInput(String raw) {
        return StringUtils.hasText(raw);
    }

    private static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    /** 允许把回调地址清空，回落到平台 notify-url/{storeId} */
    private static String trimToEmptyAllowed(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }
}
