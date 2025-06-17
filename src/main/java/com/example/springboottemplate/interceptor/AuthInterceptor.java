package com.example.springboottemplate.interceptor;

import com.example.springboottemplate.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;  // 注入 JwtUtil

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 放行OPTIONS请求
        if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) {
            return true;
        }

        // 2. 放行白名单（如登录接口）
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/auth/")) {
            return true;
        }

        // 3. 获取token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("未提供认证token");
            return false;
        }

        // 4. 验证token
        token = token.substring(7);
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("无效的token");
            return false;
        }

        // 5. 解析token并将用户信息存入request
        Claims claims = jwtUtil.parseToken(token);
        Long userId = claims.get("userId", Long.class);
        String username = claims.getSubject();

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);

        return true;
    }
}
