package com.example.springboottemplate.service.feie;

import com.example.springboottemplate.entity.MallOrder;
import com.example.springboottemplate.entity.MallOrderItem;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.StorePrinter;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/** 飞鹅 58mm 小票内容。带喇叭机型用 AUDIO 标签播报。 */
final class FeieTicketBuilder {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private FeieTicketBuilder() {
    }

    static String orderTicket(Store store, MallOrder order, List<MallOrderItem> items,
                              StorePrinter printer) {
        StringBuilder sb = new StringBuilder();
        if (isOn(printer.getVoiceEnabled())) {
            sb.append("<AUDIO>新订单,").append(plainAmount(order.getPayAmount())).append("</AUDIO>");
        }
        String storeName = store == null || !StringUtils.hasText(store.getStoreName())
                ? "店铺订单" : store.getStoreName().trim();
        sb.append("<CB>").append(escape(storeName)).append("</CB><BR>");
        sb.append("时间：").append(formatTime(order.getPaidTime() != null ? order.getPaidTime() : order.getCreatedTime()))
                .append("<BR>");
        sb.append("订单：").append(nullToEmpty(order.getOrderNo())).append("<BR>");
        sb.append("--------------------------------<BR>");
        if (items != null) {
            for (MallOrderItem item : items) {
                if (item == null) {
                    continue;
                }
                String name = truncate(nullToEmpty(item.getProductName()), 16);
                int qty = item.getQuantity() == null ? 0 : item.getQuantity();
                sb.append(escape(name)).append("<BR>");
                sb.append("  x").append(qty).append("  ").append(plainAmount(item.getAmount())).append("<BR>");
            }
        }
        sb.append("--------------------------------<BR>");
        sb.append("商品金额：").append(plainAmount(order.getGoodsAmount())).append("<BR>");
        if (order.getDeliveryFee() != null && order.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("配送费：").append(plainAmount(order.getDeliveryFee())).append("<BR>");
        }
        if (order.getDiscountAmount() != null && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("优惠：-").append(plainAmount(order.getDiscountAmount())).append("<BR>");
        }
        String payWay = order.getPayChannel() != null && order.getPayChannel() == 2 ? "余额" : "微信";
        sb.append("<B>实付：").append(plainAmount(order.getPayAmount())).append(" ").append(payWay).append("</B><BR>");
        if (StringUtils.hasText(order.getContact())) {
            sb.append("电话：").append(escape(order.getContact().trim())).append("<BR>");
        }
        if (StringUtils.hasText(order.getAddress())) {
            sb.append("地址：").append(escape(order.getAddress().trim())).append("<BR>");
        }
        if (StringUtils.hasText(order.getRemark())) {
            sb.append("备注：").append(escape(order.getRemark().trim())).append("<BR>");
        }
        sb.append("<C>** 完 **</C><BR>");
        return sb.toString();
    }

    static String testTicket(String storeName, boolean voice) {
        StringBuilder sb = new StringBuilder();
        if (voice) {
            sb.append("<AUDIO>新订单,0.01</AUDIO>");
        }
        String name = StringUtils.hasText(storeName) ? storeName.trim() : "测试店铺";
        sb.append("<CB>打印机测试</CB><BR>");
        sb.append("店铺：").append(escape(name)).append("<BR>");
        sb.append("时间：").append(TIME.format(java.time.ZonedDateTime.now(SHANGHAI))).append("<BR>");
        sb.append("<C>绑定成功，可以接单打票</C><BR>");
        return sb.toString();
    }

    private static boolean isOn(Integer flag) {
        return flag != null && flag == 1;
    }

    private static String formatTime(Date date) {
        if (date == null) {
            return TIME.format(java.time.ZonedDateTime.now(SHANGHAI));
        }
        return TIME.format(date.toInstant().atZone(SHANGHAI));
    }

    private static String plainAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.stripTrailingZeros().scale() <= 0
                ? amount.toPlainString()
                : amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private static String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("<", "＜").replace(">", "＞");
    }

    private static String truncate(String text, int maxChars) {
        if (text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars);
    }

    private static String nullToEmpty(String text) {
        return text == null ? "" : text;
    }
}
