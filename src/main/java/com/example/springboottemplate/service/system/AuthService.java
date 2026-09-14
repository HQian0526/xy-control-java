package com.example.springboottemplate.service.system;

import com.example.springboottemplate.dto.Response;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    Response login(String username, String password);

    Response wxLogin(String code, String appId);

    /**
     * 用微信手机号授权 code 绑定手机号。
     * 若手机号已有账号：当前微信尚未绑号则合并进该账号并换绑 openid。
     */
    Response bindPhone(String phoneCode, HttpServletRequest request);

    Response refreshToken(String refreshToken);

    Response getAuthCode();
}
