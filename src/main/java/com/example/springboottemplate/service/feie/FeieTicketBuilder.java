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

/**
 * 飞鹅 58mm 小票。一行 32 字节（16 个汉字）。
 * 本机型不识别 AUDIO，会打成字；来单提示用票尾 PLUGIN（外置喇叭/钱箱），内置喇叭多为收单自带滴滴。
 */
final class FeieTicketBuilder {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /** 58mm 正常字体一行宽度 */
    private static final int LINE_WIDTH = 32;

    private FeieTicketBuilder() {
    }

    static String orderTicket(Store store, MallOrder order, List<MallOrderItem> items,
                              StorePrinter printer) {
        StringBuilder sb = new StringBuilder();
        String storeName = store == null || !StringUtils.hasText(store.getStoreName())
                ? "店铺订单" : store.getStoreName().trim();
        sb.append(titleLine(storeName));
        sb.append("时间：").append(formatTime(order.getPaidTime() != null ? order.getPaidTime() : order.getCreatedTime()))
                .append("<BR>");
        sb.append("订单：").append(nullToEmpty(order.getOrderNo())).append("<BR>");
        sb.append(repeat('-', LINE_WIDTH)).append("<BR>");
        if (items != null) {
            for (MallOrderItem item : items) {
                if (item == null) {
                    continue;
                }
                int qty = item.getQuantity() == null ? 0 : item.getQuantity();
                sb.append(goodsLine(nullToEmpty(item.getProductName()), qty, plainAmount(item.getAmount())));
            }
        }
        sb.append(repeat('-', LINE_WIDTH)).append("<BR>");
        sb.append("商品金额：").append(plainAmount(order.getGoodsAmount())).append("<BR>");
        if (order.getDeliveryFee() != null && order.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("配送费：").append(plainAmount(order.getDeliveryFee())).append("<BR>");
        }
        if (order.getDiscountAmount() != null && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("优惠：-").append(plainAmount(order.getDiscountAmount())).append("<BR>");
        }
        String payWay = "微信";
        if (order.getPayChannel() != null && order.getPayChannel() == 2) {
            payWay = "余额";
        } else if (order.getPayChannel() != null && order.getPayChannel() == 3) {
            payWay = "无需支付";
        }
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
        if (isOn(printer.getVoiceEnabled())) {
            sb.append("<PLUGIN>");
        }
        return sb.toString();
    }

    static String testTicket(String storeName, boolean voice) {
        StringBuilder sb = new StringBuilder();
        sb.append(titleLine("打印机测试"));
        String name = StringUtils.hasText(storeName) ? storeName.trim() : "测试店铺";
        sb.append("店铺：").append(escape(name)).append("<BR>");
        sb.append("时间：").append(TIME.format(java.time.ZonedDateTime.now(SHANGHAI))).append("<BR>");
        sb.append("<C>绑定成功，可以接单打票</C><BR>");
        if (voice) {
            sb.append("<PLUGIN>");
        }
        return sb.toString();
    }

    /**
     * 居中倍高，不倍宽，避免 5～8 个字的店名被 CB 撑换行。
     * 超过一行（16 汉字）时截断。
     */
    private static String titleLine(String storeName) {
        String name = escape(storeName);
        int maxChars = 16;
        if (name.codePointCount(0, name.length()) > maxChars) {
            name = name.substring(0, name.offsetByCodePoints(0, maxChars));
        }
        return "<C><L>" + name + "</L></C><BR>";
    }

    /** 商品名、数量、金额同一行：左名称，右 x数量 金额。 */
    private static String goodsLine(String name, int qty, String amount) {
        String right = "x" + qty + " " + amount;
        int rightWidth = displayWidth(right);
        int nameWidth = LINE_WIDTH - rightWidth;
        if (nameWidth < 2) {
            nameWidth = 2;
        }
        String left = padRight(truncateToWidth(escape(name), nameWidth), nameWidth);
        return left + right + "<BR>";
    }

    private static int displayWidth(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            width += cp > 0x7F ? 2 : 1;
            i += Character.charCount(cp);
        }
        return width;
    }

    private static String truncateToWidth(String text, int maxWidth) {
        StringBuilder out = new StringBuilder();
        int width = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            int w = cp > 0x7F ? 2 : 1;
            if (width + w > maxWidth) {
                break;
            }
            out.appendCodePoint(cp);
            width += w;
            i += Character.charCount(cp);
        }
        return out.toString();
    }

    private static String padRight(String text, int width) {
        int current = displayWidth(text);
        StringBuilder sb = new StringBuilder(text);
        while (current < width) {
            sb.append(' ');
            current++;
        }
        return sb.toString();
    }

    private static String repeat(char ch, int count) {
        return String.valueOf(ch).repeat(Math.max(0, count));
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

    private static String nullToEmpty(String text) {
        return text == null ? "" : text;
    }
}
