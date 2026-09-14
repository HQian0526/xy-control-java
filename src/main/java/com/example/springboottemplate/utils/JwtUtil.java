package com.example.springboottemplate.utils;

import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;


@Component
public class JwtUtil {
    @Value("${jwt.secret-key}")
    private String SECRET_KEY;
    @Value("${jwt.access-token-expire}")
    private long ACCESS_EXPIRE;
    @Value("${jwt.refresh-token-expire}")
    private long REFRESH_EXPIRE;

    public String generateAccessToken(long userId, String username) {
        return generateAccessToken(userId, username, null);
    }

    public String generateAccessToken(long userId, String username, String appId) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(username)
                .claim("userId", String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRE))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY);
        if (StringUtils.hasText(appId)) {
            builder.claim("appId", appId.trim());
        }
        return builder.compact();
    }

    public String generateRefreshToken(long userId, String username) {
        return generateRefreshToken(userId, username, null);
    }

    public String generateRefreshToken(long userId, String username, String appId) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(username)
                .claim("userId", String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRE))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY);
        if (StringUtils.hasText(appId)) {
            builder.claim("appId", appId.trim());
        }
        return builder.compact();
    }

    public String getAppId(Claims claims) {
        if (claims == null) {
            return null;
        }
        Object raw = claims.get("appId");
        if (raw == null) {
            return null;
        }
        String text = raw.toString().trim();
        return text.isEmpty() ? null : text;
    }

    // 解析token获取claims
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    // 验证token是否有效
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw e; // 特殊处理过期异常
        } catch (JwtException e) {
            throw new JwtException("Invalid token");
        }
    }

    // 从Token中提取用户名
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * 安全获取userId（兼容 String / Integer / Long / Number；雪花 id 用 Long）
     */
    public Long getUserId(Claims claims) {
        if (claims == null) {
            return null;
        }
        // 优先按类型读取（JJWT 可做数值转换）
        try {
            Long typed = claims.get("userId", Long.class);
            if (typed != null) {
                return typed;
            }
        } catch (Exception ignored) {
            // fall through
        }
        Object raw = claims.get("userId");
        if (raw == null) {
            return null;
        }
        if (raw instanceof Long) {
            return (Long) raw;
        }
        if (raw instanceof Number) {
            return ((Number) raw).longValue();
        }
        try {
            String text = raw.toString().trim();
            if (text.isEmpty()) {
                return null;
            }
            return Long.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Long getUserIdFromToken(String token) {
        return getUserId(parseToken(token));
    }

    /**
     * 从请求头尝试解析 userId；无 token 或无效时返回 null（游客）
     */
    public Long tryGetUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ") || header.length() <= 7) {
            return null;
        }
        try {
            return getUserId(parseToken(header.substring(7)));
        } catch (Exception e) {
            return null;
        }
    }
}
