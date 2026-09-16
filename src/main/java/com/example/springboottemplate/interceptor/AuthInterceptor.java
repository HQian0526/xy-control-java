package com.example.springboottemplate.interceptor;

import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.mapper.system.UserMapper;
import com.example.springboottemplate.utils.JwtUtil;
import com.example.springboottemplate.utils.StoreExpireHelper;
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
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StoreMapper storeMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 放行OPTIONS请求
        if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) {
            return true;
        }

        // 2. 放行白名单（如登录接口、图片预览）；/wx/bindPhone 需要鉴权
        String uri = request.getRequestURI();
        if (uri.contains("/auth/")
                || uri.endsWith("/wx/login")
                || uri.contains("/mallOrder/wxPayNotify")
                || uri.contains("/mallOrder/wxRefundNotify")
                || uri.contains("/upload-images/")
                || uri.contains("/catagory/findCatagory")
                || uri.contains("/product/findProduct")
                || uri.contains("/otherBusiness/findOtherBusiness")
                || uri.contains("/store/findStore")
                || uri.contains("/mallPromo/active")
                || uri.contains("/mallCoupon/storeTemplates")) {
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
        Long userId = jwtUtil.getUserId(claims);
        String username = claims.getSubject();

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("appId", jwtUtil.getAppId(claims));

        // PC 后台拉菜单：租用到期的商家踢回登录。小程序不走 /menu，不受影响。
        if (uri.contains("/menu/")) {
            User user = userId == null ? null : userMapper.selectById(userId);
            if (StoreExpireHelper.isMerchantAccountExpired(user, storeMapper)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"" + StoreExpireHelper.EXPIRED_LOGIN_MSG + "\"}");
                return false;
            }
        }

        return true;
    }
}
