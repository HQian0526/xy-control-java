package com.example.springboottemplate.service.system;

import com.example.springboottemplate.dto.Response;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    Response login(String username, String password);

    Response wxLogin(String code);

    /**
     * 用微信手机号授权 code 绑定手机号；若手机号已存在后台账号则合并到该账号。
     */
    Response bindPhone(String phoneCode, HttpServletRequest request);

    Response refreshToken(String refreshToken);

    Response getAuthCode();
}
