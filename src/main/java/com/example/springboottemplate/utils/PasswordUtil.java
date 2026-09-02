package com.example.springboottemplate.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 密码哈希工具。库表 password 存 BCrypt 完整串（自带 salt），形如 $2a$10$...
 * 兼容历史明文：校验成功后应由调用方升级为 BCrypt。
 */
public final class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private PasswordUtil() {
    }

    /** 明文 → BCrypt 哈希（入库用） */
    public static String encode(String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return ENCODER.encode(rawPassword.trim());
    }

    /** 是否已是 BCrypt 哈希 */
    public static boolean isBcrypt(String stored) {
        if (!StringUtils.hasText(stored)) {
            return false;
        }
        String s = stored.trim();
        return s.startsWith("$2a$") || s.startsWith("$2b$") || s.startsWith("$2y$");
    }

    /**
     * 校验明文是否匹配库中存值。
     * 支持：BCrypt；历史明文（equals）。
     */
    public static boolean matches(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(storedPassword)) {
            return false;
        }
        String raw = rawPassword.trim();
        String stored = storedPassword.trim();
        if (isBcrypt(stored)) {
            return ENCODER.matches(raw, stored);
        }
        // 历史数据：明文直存
        return stored.equals(raw);
    }

    /** 登录成功后若非 BCrypt，需要重写入库 */
    public static boolean needsRehash(String storedPassword) {
        return StringUtils.hasText(storedPassword) && !isBcrypt(storedPassword);
    }

    /**
     * 未传初始密码时生成 9 位明文：前缀 + MD5(当天 yyyyMMdd) 的后 6 位。
     * 例：前缀 xy@，日期 20260902 → xy@c99819
     */
    public static String generateDefaultPassword(String prefix) {
        String p = StringUtils.hasText(prefix) ? prefix.trim() : "xy@";
        String day = LocalDate.now().format(DAY);
        String md5Hex = DigestUtils.md5DigestAsHex(day.getBytes(StandardCharsets.UTF_8));
        return p + md5Hex.substring(md5Hex.length() - 6);
    }
}
