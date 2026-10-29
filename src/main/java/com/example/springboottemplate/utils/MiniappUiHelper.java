package com.example.springboottemplate.utils;

import com.example.springboottemplate.entity.Store;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 小程序端展示权限：商城类默认全开，预约服务类默认关掉库存/地址/数量/购物车。
 */
public final class MiniappUiHelper {
    public static final int TYPE_MALL = 1;
    public static final int TYPE_BOOKING = 2;
    public static final String MALL_ORDER_BTN = "立即购买";
    public static final String BOOKING_ORDER_BTN = "立即预约";
    public static final String MALL_HOME_TAB = "逛店";
    public static final String BOOKING_HOME_TAB = "预约";
    public static final String LIST_BTN = "预定";
    private static final int ORDER_BTN_MAX = 16;
    private static final int HOME_TAB_MAX = 4;
    private static final int LIST_BTN_MAX = 6;

    private MiniappUiHelper() {
    }

    public static int normalizeType(Integer type) {
        return type != null && type == TYPE_BOOKING ? TYPE_BOOKING : TYPE_MALL;
    }

    public static String defaultOrderBtn(int type) {
        return type == TYPE_BOOKING ? BOOKING_ORDER_BTN : MALL_ORDER_BTN;
    }

    public static String defaultHomeTab(int type) {
        return type == TYPE_BOOKING ? BOOKING_HOME_TAB : MALL_HOME_TAB;
    }

    private static String clipText(String value, String fallback, int max) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        String text = value.trim();
        if (text.isEmpty()) {
            return fallback;
        }
        return text.length() > max ? text.substring(0, max) : text;
    }

    public static int defaultFlag(int type) {
        return type == TYPE_BOOKING ? 0 : 1;
    }

    public static boolean flagOn(Integer value, int type) {
        if (value == null) {
            return defaultFlag(type) == 1;
        }
        return value == 1;
    }

    public static boolean showAddress(Store store) {
        int type = store == null ? TYPE_MALL : normalizeType(store.getMiniappType());
        return flagOn(store == null ? null : store.getShowAddress(), type);
    }

    public static boolean showStock(Store store) {
        int type = store == null ? TYPE_MALL : normalizeType(store.getMiniappType());
        return flagOn(store == null ? null : store.getShowStock(), type);
    }

    public static boolean showDeliveryFee(Store store) {
        int type = store == null ? TYPE_MALL : normalizeType(store.getMiniappType());
        return flagOn(store == null ? null : store.getShowDeliveryFee(), type);
    }

    /** 查询回填：空值按类型给默认，保证小程序拿到完整权限。 */
    public static void fillDefaults(Store store) {
        if (store == null) {
            return;
        }
        int type = normalizeType(store.getMiniappType());
        store.setMiniappType(type);
        if (store.getShowStock() == null) {
            store.setShowStock(defaultFlag(type));
        }
        if (store.getShowAddress() == null) {
            store.setShowAddress(defaultFlag(type));
        }
        if (store.getShowQuantity() == null) {
            store.setShowQuantity(defaultFlag(type));
        }
        if (store.getShowCart() == null) {
            store.setShowCart(defaultFlag(type));
        }
        if (store.getShowSaleNum() == null) {
            store.setShowSaleNum(defaultFlag(type));
        }
        if (store.getShowDeliveryFee() == null) {
            store.setShowDeliveryFee(defaultFlag(type));
        }
        store.setOrderBtnText(clipText(store.getOrderBtnText(), defaultOrderBtn(type), ORDER_BTN_MAX));
        store.setHomeTabText(clipText(store.getHomeTabText(), defaultHomeTab(type), HOME_TAB_MAX));
        store.setListBtnText(clipText(store.getListBtnText(), LIST_BTN, LIST_BTN_MAX));
        if (store.getEnablePay() == null) {
            store.setEnablePay(1);
        }
        if (store.getEnableRecharge() == null) {
            store.setEnableRecharge(1);
        }
    }

    public static boolean enablePay(Store store) {
        if (store == null || store.getEnablePay() == null) {
            return true;
        }
        return store.getEnablePay() == 1;
    }

    /** 是否接入储值：需同时接入支付才有意义 */
    public static boolean enableRecharge(Store store) {
        if (store == null || store.getEnableRecharge() == null) {
            return enablePay(store);
        }
        return store.getEnableRecharge() == 1;
    }

    public static void applySave(Store patch, Integer miniappType, Integer showStock, Integer showAddress,
                                 Integer showQuantity, Integer showCart, String orderBtnText, Integer enablePay,
                                 String homeTabText, String listBtnText, Integer showSaleNum, Integer showDeliveryFee,
                                 BigDecimal deliveryFee) {
        applySave(patch, miniappType, showStock, showAddress, showQuantity, showCart, orderBtnText, enablePay,
                homeTabText, listBtnText, showSaleNum, showDeliveryFee, deliveryFee, null);
    }

    public static void applySave(Store patch, Integer miniappType, Integer showStock, Integer showAddress,
                                 Integer showQuantity, Integer showCart, String orderBtnText, Integer enablePay,
                                 String homeTabText, String listBtnText, Integer showSaleNum, Integer showDeliveryFee,
                                 BigDecimal deliveryFee, Integer enableRecharge) {
        int type = normalizeType(miniappType);
        patch.setMiniappType(type);
        patch.setShowStock(showStock == null ? defaultFlag(type) : (showStock == 1 ? 1 : 0));
        patch.setShowAddress(showAddress == null ? defaultFlag(type) : (showAddress == 1 ? 1 : 0));
        patch.setShowQuantity(showQuantity == null ? defaultFlag(type) : (showQuantity == 1 ? 1 : 0));
        patch.setShowCart(showCart == null ? defaultFlag(type) : (showCart == 1 ? 1 : 0));
        patch.setShowSaleNum(showSaleNum == null ? defaultFlag(type) : (showSaleNum == 1 ? 1 : 0));
        patch.setShowDeliveryFee(showDeliveryFee == null ? defaultFlag(type) : (showDeliveryFee == 1 ? 1 : 0));
        if (deliveryFee != null) {
            patch.setDeliveryFee(deliveryFee.compareTo(BigDecimal.ZERO) < 0
                    ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                    : deliveryFee.setScale(2, RoundingMode.HALF_UP));
        }
        patch.setOrderBtnText(clipText(orderBtnText, defaultOrderBtn(type), ORDER_BTN_MAX));
        patch.setHomeTabText(clipText(homeTabText, defaultHomeTab(type), HOME_TAB_MAX));
        patch.setListBtnText(clipText(listBtnText, LIST_BTN, LIST_BTN_MAX));
        patch.setEnablePay(enablePay != null && enablePay == 0 ? 0 : 1);
        // 储值需依附支付：支付关闭时强制关闭储值
        boolean payOn = patch.getEnablePay() != null && patch.getEnablePay() == 1;
        patch.setEnableRecharge(!payOn ? 0 : (enableRecharge != null && enableRecharge == 0 ? 0 : 1));
    }
}
