package com.example.springboottemplate.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 租户密钥落库加密。明文以 ENC: 前缀区分；无法解密时按明文兼容历史数据。
 */
@Component
public class SecretCodec {
    private static final String PREFIX = "ENC:";
    private static final int GCM_IV_LEN = 12;
    private static final int GCM_TAG_BITS = 128;

    @Value("${jwt.secret-key}")
    private String jwtSecret;

    public String encrypt(String plain) {
        if (!StringUtils.hasText(plain)) {
            return plain;
        }
        if (plain.startsWith(PREFIX)) {
            return plain;
        }
        try {
            byte[] iv = new byte[GCM_IV_LEN];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherBytes = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] packed = new byte[iv.length + cipherBytes.length];
            System.arraycopy(iv, 0, packed, 0, iv.length);
            System.arraycopy(cipherBytes, 0, packed, iv.length, cipherBytes.length);
            return PREFIX + Base64.getEncoder().encodeToString(packed);
        } catch (Exception e) {
            throw new IllegalStateException("密钥加密失败", e);
        }
    }

    public String decrypt(String stored) {
        if (!StringUtils.hasText(stored) || !stored.startsWith(PREFIX)) {
            return stored;
        }
        try {
            byte[] packed = Base64.getDecoder().decode(stored.substring(PREFIX.length()));
            if (packed.length <= GCM_IV_LEN) {
                return stored;
            }
            byte[] iv = new byte[GCM_IV_LEN];
            byte[] cipherBytes = new byte[packed.length - GCM_IV_LEN];
            System.arraycopy(packed, 0, iv, 0, GCM_IV_LEN);
            System.arraycopy(packed, GCM_IV_LEN, cipherBytes, 0, cipherBytes.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return stored;
        }
    }

    private SecretKeySpec aesKey() throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(String.valueOf(jwtSecret).getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(hash, "AES");
    }
}
