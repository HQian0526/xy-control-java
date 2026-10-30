package com.example.springboottemplate.service.wx;

import com.example.springboottemplate.config.WxPayProperties;
import com.example.springboottemplate.entity.MallOrder;
import com.example.springboottemplate.entity.MallOrderItem;
import com.example.springboottemplate.entity.StoreBalanceRecharge;
import com.example.springboottemplate.entity.StoreMemberRecharge;
import com.example.springboottemplate.mapper.MallOrderItemMapper;
import com.example.springboottemplate.mapper.MallOrderMapper;
import com.example.springboottemplate.mapper.StoreBalanceRechargeMapper;
import com.example.springboottemplate.mapper.StoreMemberRechargeMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 支付成功后向微信发货信息管理录入发货信息。
 * 商城实物单用 yml 的 logistics-type；储值单固定报虚拟商品（3）。
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
    /** 储值没有实物配送，按微信虚拟商品上报 */
    public static final int LOGISTICS_VIRTUAL = 3;
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
    private StoreMemberRechargeMapper storeMemberRechargeMapper;
    @Autowired
    private StoreBalanceRechargeMapper storeBalanceRechargeMapper;
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

    @Async("wxShippingExecutor")
    public void uploadRechargeShippingAsync(String rechargeNo) {
        try {
            uploadRechargeShipping(rechargeNo);
        } catch (Exception e) {
            log.error("微信储值发货异步任务异常 rechargeNo={}", rechargeNo, e);
        }
    }

    @Async("wxShippingExecutor")
    public void uploadStoreBalanceShippingAsync(String rechargeNo) {
        try {
            uploadStoreBalanceShipping(rechargeNo);
        } catch (Exception e) {
            log.error("微信店铺余额储值发货异步任务异常 rechargeNo={}", rechargeNo, e);
        }
    }

    @Scheduled(fixedDelayString = "${wechat.pay.shipping.retry-interval-ms:120000}", initialDelay = 30000)
    public void retryPendingShipping() {
        if (!isShippingEnabled()) {
            return;
        }
        WxPayProperties.Shipping shipping = wxPayProperties.getShipping();
        int batch = Math.max(1, shipping.getRetryBatchSize());
        List<MallOrder> pendingOrders = mallOrderMapper.selectPendingWxShipping(shipping.getMaxRetry(), batch);
        for (MallOrder order : pendingOrders) {
            try {
                uploadShipping(order.getOrderNo());
            } catch (Exception e) {
                log.error("微信发货重试异常 orderNo={}", order.getOrderNo(), e);
            }
        }
        List<StoreMemberRecharge> pendingRecharges =
                storeMemberRechargeMapper.selectPendingWxShipping(shipping.getMaxRetry(), batch);
        for (StoreMemberRecharge recharge : pendingRecharges) {
            try {
                uploadRechargeShipping(recharge.getRechargeNo());
            } catch (Exception e) {
                log.error("微信储值发货重试异常 rechargeNo={}", recharge.getRechargeNo(), e);
            }
        }
        List<StoreBalanceRecharge> pendingBalances =
                storeBalanceRechargeMapper.selectPendingWxShipping(shipping.getMaxRetry(), batch);
        for (StoreBalanceRecharge recharge : pendingBalances) {
            try {
                uploadStoreBalanceShipping(recharge.getRechargeNo());
            } catch (Exception e) {
                log.error("微信店铺余额储值发货重试异常 rechargeNo={}", recharge.getRechargeNo(), e);
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
        if (order.getPayChannel() == null || order.getPayChannel() != 1) {
            return;
        }
        if (order.getWxShippingStatus() != null && order.getWxShippingStatus() == STATUS_SUCCESS) {
            return;
        }

        int retry = order.getWxShippingRetry() == null ? 0 : order.getWxShippingRetry();
        int logisticsType = wxPayProperties.getShipping().getLogisticsType();
        List<MallOrderItem> items = mallOrderItemMapper.selectByOrderId(order.getId());
        doUpload(new UploadCommand(
                order.getOrderNo(),
                order.getOpenid(),
                order.getAppId(),
                order.getMchId(),
                logisticsType,
                buildItemDesc(items),
                retry,
                result -> mallOrderMapper.updateWxShippingSync(
                        order.getOrderNo(), result.status, result.errcode, result.errmsg,
                        result.retry, result.shippingTime)
        ));
    }

    public void uploadRechargeShipping(String rechargeNo) {
        if (!StringUtils.hasText(rechargeNo) || !isShippingEnabled()) {
            return;
        }
        StoreMemberRecharge recharge = storeMemberRechargeMapper.selectByRechargeNo(rechargeNo.trim());
        if (recharge == null || recharge.getPayStatus() == null || recharge.getPayStatus() != 1) {
            return;
        }
        if (recharge.getWxShippingStatus() != null && recharge.getWxShippingStatus() == STATUS_SUCCESS) {
            return;
        }

        int retry = recharge.getWxShippingRetry() == null ? 0 : recharge.getWxShippingRetry();
        doUpload(new UploadCommand(
                recharge.getRechargeNo(),
                recharge.getOpenid(),
                recharge.getAppId(),
                recharge.getMchId(),
                LOGISTICS_VIRTUAL,
                buildRechargeItemDesc(recharge.getAmount()),
                retry,
                result -> storeMemberRechargeMapper.updateWxShippingSync(
                        recharge.getRechargeNo(), result.status, result.errcode, result.errmsg,
                        result.retry, result.shippingTime)
        ));
    }

    public void uploadStoreBalanceShipping(String rechargeNo) {
        if (!StringUtils.hasText(rechargeNo) || !isShippingEnabled()) {
            return;
        }
        StoreBalanceRecharge recharge = storeBalanceRechargeMapper.selectByRechargeNo(rechargeNo.trim());
        if (recharge == null || recharge.getPayStatus() == null || recharge.getPayStatus() != 1) {
            return;
        }
        if (recharge.getWxShippingStatus() != null && recharge.getWxShippingStatus() == STATUS_SUCCESS) {
            return;
        }
        int retry = recharge.getWxShippingRetry() == null ? 0 : recharge.getWxShippingRetry();
        doUpload(new UploadCommand(
                recharge.getRechargeNo(),
                recharge.getOpenid(),
                recharge.getAppId(),
                recharge.getMchId(),
                LOGISTICS_VIRTUAL,
                "店铺余额储值" + (recharge.getAmount() == null ? "" : recharge.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString() + "元"),
                retry,
                result -> storeBalanceRechargeMapper.updateWxShippingSync(
                        recharge.getRechargeNo(), result.status, result.errcode, result.errmsg,
                        result.retry, result.shippingTime)
        ));
    }

    private void doUpload(UploadCommand command) {
        int maxRetry = Math.max(1, wxPayProperties.getShipping().getMaxRetry());
        Date now = new Date();

        if (!StringUtils.hasText(command.openid)) {
            command.saver.accept(SyncResult.failed(null, "订单缺少 openid，无法向微信报发货", maxRetry, now));
            log.error("微信发货缺少 openid outTradeNo={}", command.outTradeNo);
            return;
        }
        String mchId = StringUtils.hasText(command.mchId) ? command.mchId : wxPayProperties.getMchId();
        if (!StringUtils.hasText(mchId)) {
            command.saver.accept(SyncResult.failed(null, "未配置微信支付商户号 mch-id", maxRetry, now));
            return;
        }

        Map<String, Object> payload = buildPayload(
                command.outTradeNo, command.openid, command.logisticsType, command.itemDesc, mchId);
        try {
            JsonNode node = wxApiService.postWithAccessToken(UPLOAD_SHIPPING_URL, payload, command.appId);
            int errcode = node.path("errcode").asInt(0);
            String errmsg = node.path("errmsg").asText("ok");
            if (errcode == 0 || ALREADY_SHIPPED_CODES.contains(errcode)) {
                command.saver.accept(new SyncResult(STATUS_SUCCESS, errcode, errmsg, command.retry, now));
                log.info("微信发货同步成功 outTradeNo={} logisticsType={} errcode={}",
                        command.outTradeNo, command.logisticsType, errcode);
                return;
            }
            int nextRetry = command.retry + 1;
            boolean giveUp = NON_RETRYABLE_CODES.contains(errcode) || nextRetry >= maxRetry;
            command.saver.accept(SyncResult.failed(errcode, truncateMsg(errmsg),
                    giveUp ? Math.max(nextRetry, maxRetry) : nextRetry, now));
            log.warn("微信发货同步失败 outTradeNo={} logisticsType={} errcode={} errmsg={} retry={}/{}",
                    command.outTradeNo, command.logisticsType, errcode, errmsg, nextRetry, maxRetry);
        } catch (Exception e) {
            int nextRetry = command.retry + 1;
            command.saver.accept(SyncResult.failed(null, truncateMsg(e.getMessage()),
                    Math.min(nextRetry, maxRetry), now));
            log.warn("微信发货调用异常 outTradeNo={} logisticsType={} retry={}/{} msg={}",
                    command.outTradeNo, command.logisticsType, nextRetry, maxRetry, e.getMessage());
        }
    }

    private boolean isShippingEnabled() {
        WxPayProperties.Shipping shipping = wxPayProperties.getShipping();
        if (shipping == null || !shipping.isEnabled()) {
            return false;
        }
        return !wxPayClientService.isMock();
    }

    private Map<String, Object> buildPayload(String outTradeNo, String openid, int logisticsType,
                                             String itemDesc, String mchId) {
        Map<String, Object> orderKey = new LinkedHashMap<>();
        orderKey.put("order_number_type", 1);
        orderKey.put("mchid", mchId);
        orderKey.put("out_trade_no", outTradeNo);

        Map<String, Object> shippingItem = new LinkedHashMap<>();
        shippingItem.put("item_desc", itemDesc);

        Map<String, Object> payer = new LinkedHashMap<>();
        payer.put("openid", openid);

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

    private String buildRechargeItemDesc(BigDecimal amount) {
        if (amount == null) {
            return "会员储值";
        }
        return "会员储值¥" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String truncateMsg(String msg) {
        if (msg == null) {
            return null;
        }
        return msg.length() <= 500 ? msg : msg.substring(0, 500);
    }

    private static final class UploadCommand {
        private final String outTradeNo;
        private final String openid;
        private final String appId;
        private final String mchId;
        private final int logisticsType;
        private final String itemDesc;
        private final int retry;
        private final Consumer<SyncResult> saver;

        private UploadCommand(String outTradeNo, String openid, String appId, String mchId,
                              int logisticsType, String itemDesc, int retry, Consumer<SyncResult> saver) {
            this.outTradeNo = outTradeNo;
            this.openid = openid;
            this.appId = appId;
            this.mchId = mchId;
            this.logisticsType = logisticsType;
            this.itemDesc = itemDesc;
            this.retry = retry;
            this.saver = saver;
        }
    }

    private static final class SyncResult {
        private final int status;
        private final Integer errcode;
        private final String errmsg;
        private final int retry;
        private final Date shippingTime;

        private SyncResult(int status, Integer errcode, String errmsg, int retry, Date shippingTime) {
            this.status = status;
            this.errcode = errcode;
            this.errmsg = errmsg;
            this.retry = retry;
            this.shippingTime = shippingTime;
        }

        private static SyncResult failed(Integer errcode, String errmsg, int retry, Date shippingTime) {
            return new SyncResult(STATUS_FAILED, errcode, errmsg, retry, shippingTime);
        }
    }
}
