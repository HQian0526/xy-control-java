package com.example.springboottemplate.service.wx;

import com.example.springboottemplate.config.WxPayProperties;
import com.example.springboottemplate.entity.MallOrder;
import com.example.springboottemplate.entity.MallOrderItem;
import com.example.springboottemplate.mapper.MallOrderItemMapper;
import com.example.springboottemplate.mapper.MallOrderMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 支付成功后向微信发货信息管理录入发货信息（同城配送/自提）。
 */
@Slf4j
@Service
public class WxShippingService {

    private static final String UPLOAD_SHIPPING_URL =
            "https://api.weixin.qq.com/wxa/sec/order/upload_shipping_info";
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter RFC3339 =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final int ITEM_DESC_MAX = 120;
    /** 已发货 / 信息未更新，视为同步成功 */
    private static final Set<Integer> ALREADY_SHIPPED_CODES = Set.of(10060002, 10060003, 10060023);
    /** 参数/openid 等错误，再试也没用 */
    private static final Set<Integer> NON_RETRYABLE_CODES = Set.of(
            10060005, 10060006, 10060008, 10060009, 10060020, 10060031,
            268485194, 268485196, 268485197, 268485216, 268485224, 268485228
    );

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAILED = 2;

    @Autowired
    private MallOrderMapper mallOrderMapper;
    @Autowired
    private MallOrderItemMapper mallOrderItemMapper;
    @Autowired
    private WxApiService wxApiService;
    @Autowired
    private WxPayClientService wxPayClientService;
    @Autowired
    private WxPayProperties wxPayProperties;

    @Async("wxShippingExecutor")
    public void uploadShippingAsync(String orderNo) {
        try {
            uploadShipping(orderNo);
        } catch (Exception e) {
            log.error("微信发货异步任务异常 orderNo={}", orderNo, e);
        }
    }

    @Scheduled(fixedDelayString = "${wechat.pay.shipping.retry-interval-ms:120000}", initialDelay = 30000)
    public void retryPendingShipping() {
        if (!isShippingEnabled()) {
            return;
        }
        WxPayProperties.Shipping shipping = wxPayProperties.getShipping();
        int batch = Math.max(1, shipping.getRetryBatchSize());
        List<MallOrder> pending = mallOrderMapper.selectPendingWxShipping(shipping.getMaxRetry(), batch);
        for (MallOrder order : pending) {
            try {
                uploadShipping(order.getOrderNo());
            } catch (Exception e) {
                log.error("微信发货重试异常 orderNo={}", order.getOrderNo(), e);
            }
        }
    }

    public void uploadShipping(String orderNo) {
        if (!StringUtils.hasText(orderNo) || !isShippingEnabled()) {
            return;
        }
        MallOrder order = mallOrderMapper.selectByOrderNo(orderNo.trim());
        if (order == null || order.getPayStatus() == null || order.getPayStatus() != 1) {
            return;
        }
        if (order.getWxShippingStatus() != null && order.getWxShippingStatus() == STATUS_SUCCESS) {
            return;
        }

        int retry = order.getWxShippingRetry() == null ? 0 : order.getWxShippingRetry();
        int maxRetry = Math.max(1, wxPayProperties.getShipping().getMaxRetry());
        int logisticsType = wxPayProperties.getShipping().getLogisticsType();
        Date now = new Date();

        if (logisticsType != 2 && logisticsType != 4) {
            saveSync(orderNo, STATUS_FAILED, null,
                    "wechat.pay.shipping.logistics-type 仅支持 2(同城配送) 或 4(用户自提)，当前=" + logisticsType,
                    maxRetry, now);
            log.error("微信发货配置非法 orderNo={} logisticsType={}", orderNo, logisticsType);
            return;
        }
        if (!StringUtils.hasText(order.getOpenid())) {
            saveSync(orderNo, STATUS_FAILED, null, "订单缺少 openid，无法向微信报发货", maxRetry, now);
            log.error("微信发货缺少 openid orderNo={}", orderNo);
            return;
        }
        if (!StringUtils.hasText(wxPayProperties.getMchId())) {
            saveSync(orderNo, STATUS_FAILED, null, "未配置微信支付商户号 mch-id", maxRetry, now);
            return;
        }

        List<MallOrderItem> items = mallOrderItemMapper.selectByOrderId(order.getId());
        String itemDesc = buildItemDesc(items);
        Map<String, Object> payload = buildPayload(order, logisticsType, itemDesc);

        try {
            JsonNode node = wxApiService.postWithAccessToken(UPLOAD_SHIPPING_URL, payload);
            int errcode = node.path("errcode").asInt(0);
            String errmsg = node.path("errmsg").asText("ok");
            if (errcode == 0 || ALREADY_SHIPPED_CODES.contains(errcode)) {
                saveSync(orderNo, STATUS_SUCCESS, errcode, errmsg, retry, now);
                log.info("微信发货同步成功 orderNo={} errcode={}", orderNo, errcode);
                return;
            }
            int nextRetry = retry + 1;
            boolean giveUp = NON_RETRYABLE_CODES.contains(errcode) || nextRetry >= maxRetry;
            saveSync(orderNo, STATUS_FAILED, errcode, truncateMsg(errmsg),
                    giveUp ? Math.max(nextRetry, maxRetry) : nextRetry, now);
            log.warn("微信发货同步失败 orderNo={} errcode={} errmsg={} retry={}/{}",
                    orderNo, errcode, errmsg, nextRetry, maxRetry);
        } catch (Exception e) {
            int nextRetry = retry + 1;
            saveSync(orderNo, STATUS_FAILED, null, truncateMsg(e.getMessage()),
                    Math.min(nextRetry, maxRetry), now);
            log.warn("微信发货调用异常 orderNo={} retry={}/{} msg={}",
                    orderNo, nextRetry, maxRetry, e.getMessage());
        }
    }

    private boolean isShippingEnabled() {
        WxPayProperties.Shipping shipping = wxPayProperties.getShipping();
        if (shipping == null || !shipping.isEnabled()) {
            return false;
        }
        return !wxPayClientService.isMock();
    }

    private Map<String, Object> buildPayload(MallOrder order, int logisticsType, String itemDesc) {
        Map<String, Object> orderKey = new LinkedHashMap<>();
        orderKey.put("order_number_type", 1);
        orderKey.put("mchid", wxPayProperties.getMchId());
        orderKey.put("out_trade_no", order.getOrderNo());

        Map<String, Object> shippingItem = new LinkedHashMap<>();
        shippingItem.put("item_desc", itemDesc);

        Map<String, Object> payer = new LinkedHashMap<>();
        payer.put("openid", order.getOpenid());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("order_key", orderKey);
        payload.put("logistics_type", logisticsType);
        payload.put("delivery_mode", 1);
        payload.put("shipping_list", List.of(shippingItem));
        payload.put("upload_time", OffsetDateTime.now(SHANGHAI).format(RFC3339));
        payload.put("payer", payer);
        return payload;
    }

    private String buildItemDesc(List<MallOrderItem> items) {
        if (items == null || items.isEmpty()) {
            return "商城订单";
        }
        StringBuilder sb = new StringBuilder();
        for (MallOrderItem item : items) {
            if (sb.length() > 0) {
                sb.append("、");
            }
            String name = StringUtils.hasText(item.getProductName()) ? item.getProductName().trim() : "商品";
            int qty = item.getQuantity() == null || item.getQuantity() <= 0 ? 1 : item.getQuantity();
            sb.append(name).append("*").append(qty);
            if (sb.length() >= ITEM_DESC_MAX) {
                break;
            }
        }
        String desc = sb.toString();
        if (!StringUtils.hasText(desc)) {
            return "商城订单";
        }
        return desc.length() <= ITEM_DESC_MAX ? desc : desc.substring(0, ITEM_DESC_MAX);
    }

    private void saveSync(String orderNo, int status, Integer errcode, String errmsg, int retry, Date shippingTime) {
        mallOrderMapper.updateWxShippingSync(orderNo, status, errcode, errmsg, retry, shippingTime);
    }

    private String truncateMsg(String msg) {
        if (msg == null) {
            return null;
        }
        return msg.length() <= 500 ? msg : msg.substring(0, 500);
    }
}
