package com.example.springboottemplate.utils;

import org.springframework.util.DigestUtils;

import java.util.UUID;

public class PasswordUtil {
    // 生成盐值
    public static String generateSalt() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 20);
    }

    // 加密密码（盐值+密码）
    public static String encryptPassword(String password, String salt) {
        return DigestUtils.md5DigestAsHex((salt + password).getBytes());
    }

    // 验证密码
    public static boolean verifyPassword(String inputPassword, String salt, String dbPassword) {
        String encryptedInput = encryptPassword(inputPassword, salt);
        return encryptedInput.equals(dbPassword);
    }
}
