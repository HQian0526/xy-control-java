package com.example.springboottemplate.controller.system;

import com.example.springboottemplate.dto.RefreshToken;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.service.system.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
@Api(tags = "登录令牌", description = "登录权限相关接口")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @ResponseBody
    @ApiOperation(value = "登录", notes = "获取登录token")
    public Response login(@RequestBody User user) {
        return authService.login(user.getUserName(), user.getPassword());
    }

    @PostMapping("/refreshToken")
    @ResponseBody
    @ApiOperation(value = "刷新令牌", notes = "刷新登录token")
    public Response refreshToken(@RequestBody RefreshToken refreshToken) {
        return authService.refreshToken(refreshToken.getRefreshToken());
    }

    //获取当前登录用户的权限编码，用于权限控制
    @GetMapping("/codes")
    @ResponseBody
    @ApiOperation(value = "查询当前登录用户权限编码", notes = "用于权限控制")
    public Response getAuthCode() {
        return authService.getAuthCode();
    }
}
