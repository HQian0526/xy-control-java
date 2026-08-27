package com.example.springboottemplate.utils;

import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret-key}")
    private String SECRET_KEY;
    @Value("${jwt.access-token-expire}")
    private long ACCESS_EXPIRE;
    @Value("${jwt.refresh-token-expire}")
    private long REFRESH_EXPIRE;

    // 生成accessToken
    public String generateAccessToken(int userId, String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRE))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // 生成refreshToken
    public String generateRefreshToken(int userId, String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRE))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
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
     * 安全获取userId（兼容 Integer / Long / Number / 字符串）
     */
    public Integer getUserId(Claims claims) {
        Object raw = claims.get("userId");
        if (raw == null) {
            return null;
        }
        if (raw instanceof Integer) {
            return (Integer) raw;
        }
        if (raw instanceof Number) {
            return ((Number) raw).intValue();
        }
        try {
            return Integer.valueOf(raw.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Integer getUserIdFromToken(String token) {
        return getUserId(parseToken(token));
    }

    /**
     * 从请求头尝试解析 userId；无 token 或无效时返回 null（游客）
     */
    public Integer tryGetUserId(HttpServletRequest request) {
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
