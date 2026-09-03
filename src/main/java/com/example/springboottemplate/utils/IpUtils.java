package com.example.springboottemplate.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

/**
 * 获取客户端真实 IP（仅返回 IPv4；兼容 Nginx 反代）。
 */
public final class IpUtils {

    private IpUtils() {
    }

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String[] candidates = {
                request.getHeader("X-Forwarded-For"),
                request.getHeader("X-Real-IP"),
                request.getHeader("Proxy-Client-IP"),
                request.getHeader("WL-Proxy-Client-IP"),
                request.getRemoteAddr()
        };
        for (String candidate : candidates) {
            String ipv4 = firstIpv4(candidate);
            if (StringUtils.hasText(ipv4)) {
                return ipv4;
            }
        }
        return "";
    }

    /**
     * 从可能含多个 IP 的头中取第一个可用的 IPv4；
     * IPv6 回环 → 127.0.0.1；IPv4 映射地址 → 内嵌 IPv4；纯 IPv6 丢弃。
     */
    public static String firstIpv4(String raw) {
        if (!StringUtils.hasText(raw) || "unknown".equalsIgnoreCase(raw.trim())) {
            return "";
        }
        String[] parts = raw.split(",");
        for (String part : parts) {
            String ipv4 = toIpv4(part.trim());
            if (StringUtils.hasText(ipv4)) {
                return ipv4;
            }
        }
        return "";
    }

    /** 转为 IPv4 字符串；无法表示则返回空 */
    public static String toIpv4(String ip) {
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            return "";
        }
        String value = ip.trim();
        if (value.startsWith("[") && value.contains("]")) {
            value = value.substring(1, value.indexOf(']'));
        }
        // 去掉偶发端口：1.2.3.4:5678（IPv4）
        if (value.matches("^\\d{1,3}(\\.\\d{1,3}){3}:\\d+$")) {
            value = value.substring(0, value.lastIndexOf(':'));
        }
        try {
            InetAddress addr = InetAddress.getByName(value);
            if (addr.isLoopbackAddress()) {
                return "127.0.0.1";
            }
            if (addr instanceof Inet4Address) {
                return addr.getHostAddress();
            }
            if (addr instanceof Inet6Address) {
                byte[] bytes = addr.getAddress();
                // IPv4-mapped IPv6: ::ffff:a.b.c.d
                if (bytes.length == 16
                        && bytes[0] == 0 && bytes[1] == 0 && bytes[2] == 0 && bytes[3] == 0
                        && bytes[4] == 0 && bytes[5] == 0 && bytes[6] == 0 && bytes[7] == 0
                        && bytes[8] == 0 && bytes[9] == 0
                        && (bytes[10] & 0xff) == 0xff && (bytes[11] & 0xff) == 0xff) {
                    return String.format("%d.%d.%d.%d",
                            bytes[12] & 0xff, bytes[13] & 0xff, bytes[14] & 0xff, bytes[15] & 0xff);
                }
            }
        } catch (Exception ignored) {
            // fall through
        }
        // 已是标准 IPv4 字面量时再兜底校验
        if (value.matches("^\\d{1,3}(\\.\\d{1,3}){3}$")) {
            return value;
        }
        return "";
    }

    /** 暂无属地库：内网标记，外网留空（后续可接 ip2region） */
    public static String resolveLocation(String ip) {
        String ipv4 = toIpv4(ip);
        if (!StringUtils.hasText(ipv4)) {
            return "";
        }
        if (isInternalIp(ipv4)) {
            return "内网IP";
        }
        return "";
    }

    public static boolean isInternalIp(String ip) {
        String value = toIpv4(ip);
        if (!StringUtils.hasText(value)) {
            return false;
        }
        return "127.0.0.1".equals(value)
                || value.startsWith("10.")
                || value.startsWith("192.168.")
                || value.matches("^172\\.(1[6-9]|2\\d|3[0-1])\\..*");
    }
}
