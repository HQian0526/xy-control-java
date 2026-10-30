package com.example.springboottemplate.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * 识别操作来源：PC 管理端 / 微信小程序。
 * 优先读客户端显式头 X-Client，其次 Referer / User-Agent / 微信登录接口。
 */
public final class ClientSourceHelper {

    public static final String HEADER = "X-Client";
    public static final String PC = "pc";
    public static final String MINIAPP = "miniapp";

    private ClientSourceHelper() {
    }

    public static String resolve(HttpServletRequest request) {
        if (request == null) {
            return PC;
        }
        String fromHeader = normalize(firstHeader(request, HEADER, "X-Client-Type", "X-Platform"));
        if (fromHeader != null) {
            return fromHeader;
        }
        if (isMiniappByReferer(request.getHeader("Referer"))
                || isMiniappByUserAgent(request.getHeader("User-Agent"))
                || isMiniappByUri(request.getRequestURI())) {
            return MINIAPP;
        }
        return PC;
    }

    private static String normalize(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String v = raw.trim().toLowerCase();
        if (v.contains("mini") || "mp".equals(v) || v.contains("wechat") || v.equals("wx")) {
            return MINIAPP;
        }
        if (v.contains("pc") || v.contains("admin") || v.contains("web")) {
            return PC;
        }
        return null;
    }

    private static boolean isMiniappByUserAgent(String ua) {
        if (!StringUtils.hasText(ua)) {
            return false;
        }
        String u = ua.toLowerCase();
        return u.contains("miniprogram")
                || u.contains("miniprogramenv")
                || u.contains("wechatdevtools");
    }

    private static boolean isMiniappByReferer(String referer) {
        if (!StringUtils.hasText(referer)) {
            return false;
        }
        String r = referer.toLowerCase();
        return r.contains("servicewechat.com") || r.contains("serviceweixin.com");
    }

    private static boolean isMiniappByUri(String uri) {
        if (!StringUtils.hasText(uri)) {
            return false;
        }
        String path = uri.toLowerCase();
        return path.contains("/wx/login") || path.contains("/wx/bindphone");
    }

    private static String firstHeader(HttpServletRequest request, String... names) {
        for (String name : names) {
            String value = request.getHeader(name);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }
}
