package com.example.springboottemplate.utils;

import com.example.springboottemplate.dto.StoreBusinessHours;
import com.example.springboottemplate.dto.StoreBusinessHoursRule;
import com.example.springboottemplate.entity.Store;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * 店铺是否可下单：手动打烊优先；未配置营业时间视为全天可下单；时间段不跨天。
 */
public final class StoreOpenHelper {
    public static final int STORE_STATUS_CLOSED = 2;
    public static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private static final Pattern HH_MM = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] WEEKDAY_LABELS = {
            "", "周一", "周二", "周三", "周四", "周五", "周六", "周日"
    };

    private StoreOpenHelper() {
    }

    public static boolean isAcceptingOrders(Store store) {
        return isAcceptingOrders(store, ZonedDateTime.now(SHANGHAI));
    }

    public static boolean isAcceptingOrders(Store store, ZonedDateTime now) {
        if (store == null) {
            return true;
        }
        if (store.getStoreStatus() != null && store.getStoreStatus() == STORE_STATUS_CLOSED) {
            return false;
        }
        StoreBusinessHours hours = parse(store.getBusinessHours());
        List<StoreBusinessHoursRule> rules = validRules(hours);
        if (rules.isEmpty()) {
            return true;
        }
        int day = now.getDayOfWeek().getValue();
        LocalTime time = now.toLocalTime();
        for (StoreBusinessHoursRule rule : rules) {
            if (!rule.getDays().contains(day)) {
                continue;
            }
            LocalTime start = LocalTime.parse(rule.getStart(), TIME_FMT);
            LocalTime end = LocalTime.parse(rule.getEnd(), TIME_FMT);
            if (!time.isBefore(start) && time.isBefore(end)) {
                return true;
            }
        }
        return false;
    }

    public static String rejectMessage(Store store) {
        if (store != null && store.getStoreStatus() != null && store.getStoreStatus() == STORE_STATUS_CLOSED) {
            return "店铺已打烊，请于营业时间下单";
        }
        String text = formatText(store == null ? null : store.getBusinessHours());
        if (StringUtils.hasText(text)) {
            return "当前不在营业时间内（" + text + "）";
        }
        return "当前不在营业时间内";
    }

    public static String formatText(String json) {
        List<StoreBusinessHoursRule> rules = validRules(parse(json));
        if (rules.isEmpty()) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (StoreBusinessHoursRule rule : rules) {
            parts.add(formatDays(rule.getDays()) + " " + rule.getStart() + "-" + rule.getEnd());
        }
        return String.join("；", parts);
    }

    /**
     * 校验并规范化后写回 JSON。rules 为空表示明确不限时间。
     */
    public static String normalizeToJson(StoreBusinessHours hours) {
        List<StoreBusinessHoursRule> source = hours == null || hours.getRules() == null
                ? Collections.emptyList()
                : hours.getRules();
        StoreBusinessHours normalized = new StoreBusinessHours();
        List<StoreBusinessHoursRule> rules = new ArrayList<>();
        for (StoreBusinessHoursRule rule : source) {
            if (rule == null) {
                continue;
            }
            StoreBusinessHoursRule next = normalizeRule(rule);
            if (next != null) {
                rules.add(next);
            }
        }
        normalized.setRules(rules);
        return JsonUtil.toJson(normalized);
    }

    public static StoreBusinessHours parse(String json) {
        if (!StringUtils.hasText(json)) {
            return emptyHours();
        }
        try {
            StoreBusinessHours hours = JsonUtil.fromJson(json.trim(), StoreBusinessHours.class);
            return hours == null ? emptyHours() : hours;
        } catch (RuntimeException ignored) {
            return emptyHours();
        }
    }

    private static StoreBusinessHours emptyHours() {
        StoreBusinessHours hours = new StoreBusinessHours();
        hours.setRules(new ArrayList<>());
        return hours;
    }

    private static List<StoreBusinessHoursRule> validRules(StoreBusinessHours hours) {
        if (hours == null || hours.getRules() == null) {
            return Collections.emptyList();
        }
        List<StoreBusinessHoursRule> result = new ArrayList<>();
        for (StoreBusinessHoursRule rule : hours.getRules()) {
            if (isUsableRule(rule)) {
                result.add(rule);
            }
        }
        return result;
    }

    private static boolean isUsableRule(StoreBusinessHoursRule rule) {
        if (rule == null || rule.getDays() == null || rule.getDays().isEmpty()) {
            return false;
        }
        if (!isHhMm(rule.getStart()) || !isHhMm(rule.getEnd())) {
            return false;
        }
        LocalTime start = LocalTime.parse(rule.getStart(), TIME_FMT);
        LocalTime end = LocalTime.parse(rule.getEnd(), TIME_FMT);
        if (!start.isBefore(end)) {
            return false;
        }
        for (Integer day : rule.getDays()) {
            if (day == null || day < 1 || day > 7) {
                return false;
            }
        }
        return true;
    }

    private static StoreBusinessHoursRule normalizeRule(StoreBusinessHoursRule rule) {
        if (rule.getDays() == null || rule.getDays().isEmpty()) {
            throw new IllegalArgumentException("请选择营业星期");
        }
        Set<Integer> days = new TreeSet<>();
        for (Integer day : rule.getDays()) {
            if (day == null || day < 1 || day > 7) {
                throw new IllegalArgumentException("星期参数不正确");
            }
            days.add(day);
        }
        String start = normalizeTime(rule.getStart(), "开始时间");
        String end = normalizeTime(rule.getEnd(), "结束时间");
        LocalTime startTime = LocalTime.parse(start, TIME_FMT);
        LocalTime endTime = LocalTime.parse(end, TIME_FMT);
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("结束时间必须晚于开始时间，且不支持跨天");
        }
        StoreBusinessHoursRule next = new StoreBusinessHoursRule();
        next.setDays(new ArrayList<>(days));
        next.setStart(start);
        next.setEnd(end);
        return next;
    }

    private static String normalizeTime(String value, String label) {
        if (!isHhMm(value)) {
            throw new IllegalArgumentException(label + "格式不正确");
        }
        return value.trim();
    }

    private static boolean isHhMm(String value) {
        return StringUtils.hasText(value) && HH_MM.matcher(value.trim()).matches();
    }

    private static String formatDays(List<Integer> days) {
        Set<Integer> unique = new LinkedHashSet<>();
        List<Integer> sorted = new ArrayList<>();
        for (Integer day : days) {
            if (day != null && day >= 1 && day <= 7 && unique.add(day)) {
                sorted.add(day);
            }
        }
        Collections.sort(sorted);
        if (sorted.size() == 7) {
            return "每天";
        }
        if (sorted.isEmpty()) {
            return "";
        }
        if (isConsecutive(sorted)) {
            if (sorted.size() == 1) {
                return WEEKDAY_LABELS[sorted.get(0)];
            }
            return WEEKDAY_LABELS[sorted.get(0)] + "至" + WEEKDAY_LABELS[sorted.get(sorted.size() - 1)];
        }
        List<String> labels = new ArrayList<>();
        for (Integer day : sorted) {
            labels.add(WEEKDAY_LABELS[day]);
        }
        return String.join("、", labels);
    }

    private static boolean isConsecutive(List<Integer> sorted) {
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i) != sorted.get(i - 1) + 1) {
                return false;
            }
        }
        return true;
    }
}
