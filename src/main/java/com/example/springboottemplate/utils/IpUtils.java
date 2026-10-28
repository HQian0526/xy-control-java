package com.example.springboottemplate.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.lionsoul.ip2region.service.Config;
import org.lionsoul.ip2region.service.Ip2Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取客户端真实 IP（仅返回 IPv4；兼容 Nginx 反代），并用离线库解析属地。
 */
public final class IpUtils {

    private static final Logger log = LoggerFactory.getLogger(IpUtils.class);
    private static final String XDB_RESOURCE = "/ip2region/ip2region_v4.xdb";
    private static final int LOCATION_MAX = 128;

    private static volatile Ip2Region ip2Region;
    private static volatile boolean ip2RegionTried;

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

    /** 内网标记；公网用 ip2region 离线解析到省/市/运营商 */
    public static String resolveLocation(String ip) {
        String ipv4 = toIpv4(ip);
        if (!StringUtils.hasText(ipv4)) {
            return "";
        }
        if (isInternalIp(ipv4)) {
            return "内网IP";
        }
        Ip2Region searcher = ip2Region();
        if (searcher == null) {
            return "";
        }
        try {
            return formatRegion(searcher.search(ipv4));
        } catch (Exception e) {
            log.debug("解析 IP 属地失败 {}: {}", ipv4, e.getMessage());
            return "";
        }
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

    private static Ip2Region ip2Region() {
        if (ip2RegionTried) {
            return ip2Region;
        }
        synchronized (IpUtils.class) {
            if (ip2RegionTried) {
                return ip2Region;
            }
            ip2RegionTried = true;
            InputStream in = IpUtils.class.getResourceAsStream(XDB_RESOURCE);
            if (in == null) {
                log.warn("未找到 IP 属地库 {}", XDB_RESOURCE);
                return null;
            }
            try {
                Config v4 = Config.custom()
                        .setCachePolicy(Config.BufferCache)
                        .setXdbInputStream(in)
                        .asV4();
                ip2Region = Ip2Region.create(v4, null);
            } catch (Exception e) {
                log.warn("初始化 IP 属地库失败: {}", e.getMessage());
                ip2Region = null;
            } finally {
                try {
                    in.close();
                } catch (Exception ignored) {
                    // ignore
                }
            }
            return ip2Region;
        }
    }

    /**
     * 兼容 ip2region 3.x「国家|省份|城市|ISP|CN」和 2.x「国家|区域|省份|城市|ISP」。
     * 中国有省市时省略国家，例如：广东省 广州市 腾讯云
     */
    static String formatRegion(String region) {
        if (!StringUtils.hasText(region)) {
            return "";
        }
        String[] parts = region.split("\\|", -1);
        String country;
        String province;
        String city;
        String isp;
        boolean v3 = parts.length >= 5 && parts[parts.length - 1].matches("^[A-Z]{2}$");
        if (v3) {
            country = cleanPart(parts[0]);
            province = cleanPart(parts[1]);
            city = cleanPart(parts[2]);
            isp = cleanPart(parts[3]);
        } else if (parts.length >= 5) {
            country = cleanPart(parts[0]);
            province = cleanPart(parts[2]);
            city = cleanPart(parts[3]);
            isp = cleanPart(parts[4]);
        } else {
            country = parts.length > 0 ? cleanPart(parts[0]) : "";
            province = parts.length > 1 ? cleanPart(parts[1]) : "";
            city = parts.length > 2 ? cleanPart(parts[2]) : "";
            isp = parts.length > 3 ? cleanPart(parts[3]) : "";
        }

        List<String> tokens = new ArrayList<>();
        boolean china = "中国".equals(country) || "CN".equalsIgnoreCase(country);
        if (!china && StringUtils.hasText(country)) {
            tokens.add(country);
        }
        if (StringUtils.hasText(province)) {
            tokens.add(province);
        }
        if (StringUtils.hasText(city) && !city.equals(province)) {
            tokens.add(city);
        }
        if (china && tokens.isEmpty() && StringUtils.hasText(country)) {
            tokens.add(country);
        }
        if (StringUtils.hasText(isp) && !isp.equals(city) && !isp.equals(province)) {
            tokens.add(isp);
        }
        if (tokens.isEmpty()) {
            return "";
        }
        String text = String.join(" ", tokens);
        return text.length() > LOCATION_MAX ? text.substring(0, LOCATION_MAX) : text;
    }

    private static String cleanPart(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        String value = raw.trim();
        if ("0".equals(value) || "内网IP".equals(value) || "*".equals(value)) {
            return "";
        }
        return value;
    }
}
