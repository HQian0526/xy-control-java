package com.example.springboottemplate.service.system;

import com.example.springboottemplate.dto.Response;

public interface AuthService {
    Response login(String username, String password);

    Response refreshToken(String refreshToken);

    Response getAuthCode();
}
