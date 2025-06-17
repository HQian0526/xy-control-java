package com.example.springboottemplate.service;

import com.example.springboottemplate.entity.Response;

public interface AuthService {
    Response login(String username, String password);

    Response refreshToken(String refreshToken);

    Response getAuthCode();
}
