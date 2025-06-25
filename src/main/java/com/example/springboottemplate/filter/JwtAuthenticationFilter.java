package com.example.springboottemplate.filter;

import com.example.springboottemplate.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class JwtAuthenticationFilter implements Filter {
    @Autowired
    private JwtUtil jwtUtil;  // 注入 JwtUtil
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            String token = extractToken(httpRequest);

            if (token != null && jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                httpRequest.setAttribute("username", username);
            }

            chain.doFilter(request, response);

        } catch (ExpiredJwtException ex) {
            sendErrorResponse(httpResponse, 401, "Token expired");
        } catch (JwtException ex) {
            sendErrorResponse(httpResponse, 403, "Invalid token");
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, int code, String message)
            throws IOException {
        response.setStatus(code);
        response.setContentType("application/json");
        response.getWriter().write(
                String.format("{\"code\":%d,\"message\":\"%s\"}", code, message)
        );
    }
}