package com.example.springboottemplate.service.wx;

import com.example.springboottemplate.config.WxPayProperties;
import com.example.springboottemplate.entity.MallOrder;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.MallOrderMapper;
import com.example.springboottemplate.service.MallOrderService;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 待支付超时关单：查详情/列表时同步，定时任务扫尾。
 * 关单前先问微信，避免回调晚到误关。
 */
@Slf4j
@Service
public class MallOrderTimeoutService {

    @Autowired
    private MallOrderMapper mallOrderMapper;
    @Autowired
    private WxPayClientService wxPayClientService;
    @Autowired
    private WxPayProperties wxPayProperties;
    @Autowired
    @Lazy
    private MallOrderService mallOrderService;

    @Scheduled(fixedDelayString = "${wechat.pay.unpaid.scan-interval-ms:120000}", initialDelay = 45000)
    public void scanExpiredUnpaid() {
        WxPayProperties.Unpaid unpaid = wxPayProperties.getUnpaid();
        if (unpaid == null || !unpaid.isScanEnabled()) {
            return;
        }
        Date expireBefore = expireBefore();
        int batch = unpaid.resolvedScanBatchSize();
        List<MallOrder> pending = mallOrderMapper.selectExpiredUnpaid(expireBefore, batch);
        if (pending == null || pending.isEmpty()) {
            return;
        }
        for (MallOrder order : pending) {
            try {
                reconcileUnpaid(order, false);
            } catch (Exception e) {
                log.error("超时关单扫尾异常 orderNo={}", order == null ? null : order.getOrderNo(), e);
            }
        }
    }

    /**
     * @param queryWechatIfNotExpired true=查详情时即使未超时也向微信查单（补回调）
     */
    public MallOrder reconcileUnpaid(MallOrder order, boolean queryWechatIfNotExpired) {
        if (order == null || order.getPayStatus() == null || order.getPayStatus() != 0) {
            return order;
        }
        boolean expired = isExpired(order);
        if (!expired && !queryWechatIfNotExpired) {
            return order;
        }
        if (wxPayClientService.isMock()) {
            if (expired) {
                mallOrderService.closeIfUnpaid(order.getOrderNo());
                return reload(order.getOrderNo(), order);
            }
            return order;
        }

        Transaction tx;
        try {
            tx = wxPayClientService.queryByOutTradeNo(order.getOrderNo());
        } catch (BusinessException e) {
            if (expired && isOrderNotExist(e)) {
                mallOrderService.closeIfUnpaid(order.getOrderNo());
                return reload(order.getOrderNo(), order);
            }
            log.warn("超时关单查微信失败 orderNo={} msg={}", order.getOrderNo(), e.getMessage());
            return order;
        } catch (Exception e) {
            log.warn("超时关单查微信异常 orderNo={}", order.getOrderNo(), e);
            return order;
        }

        if (tx != null && tx.getTradeState() == Transaction.TradeStateEnum.SUCCESS) {
            mallOrderService.applyPaidIfUnpaid(order.getOrderNo(), tx.getTransactionId());
            return reload(order.getOrderNo(), order);
        }
        if (expired && shouldClose(tx)) {
            mallOrderService.closeIfUnpaid(order.getOrderNo());
            return reload(order.getOrderNo(), order);
        }
        return order;
    }

    public boolean isExpired(MallOrder order) {
        if (order == null || order.getCreatedTime() == null) {
            return false;
        }
        long timeoutMs = TimeUnit.MINUTES.toMillis(resolvedTimeoutMinutes());
        return order.getCreatedTime().getTime() + timeoutMs <= System.currentTimeMillis();
    }

    private Date expireBefore() {
        return new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(resolvedTimeoutMinutes()));
    }

    private int resolvedTimeoutMinutes() {
        WxPayProperties.Unpaid unpaid = wxPayProperties.getUnpaid();
        return unpaid == null ? 15 : unpaid.resolvedTimeoutMinutes();
    }

    private MallOrder reload(String orderNo, MallOrder fallback) {
        MallOrder latest = mallOrderMapper.selectByOrderNo(orderNo);
        return latest != null ? latest : fallback;
    }

    private static boolean shouldClose(Transaction tx) {
        if (tx == null || tx.getTradeState() == null) {
            return true;
        }
        Transaction.TradeStateEnum state = tx.getTradeState();
        return state == Transaction.TradeStateEnum.NOTPAY
                || state == Transaction.TradeStateEnum.CLOSED
                || state == Transaction.TradeStateEnum.REVOKED
                || state == Transaction.TradeStateEnum.PAYERROR;
    }

    private static boolean isOrderNotExist(BusinessException e) {
        String msg = e == null ? null : e.getMessage();
        if (!StringUtils.hasText(msg)) {
            return false;
        }
        String upper = msg.toUpperCase();
        return upper.contains("ORDER_NOT_EXIST")
                || upper.contains("RESOURCE_NOT_EXISTS")
                || msg.contains("订单不存在");
    }
}
