package com.example.springboottemplate.controller.system;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.WxBindPhoneRequest;
import com.example.springboottemplate.dto.WxLoginRequest;
import com.example.springboottemplate.service.system.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/wx")
@Api(tags = "微信登录", description = "微信小程序登录相关接口")
public class WxController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @ResponseBody
    @ApiOperation(value = "微信小程序登录", notes = "用 wx.login 的 code 换取系统 JWT")
    public Response login(@RequestBody WxLoginRequest request) {
        return authService.wxLogin(request == null ? null : request.getCode());
    }

    @PostMapping("/bindPhone")
    @ResponseBody
    @ApiOperation(value = "绑定微信手机号", notes = "需登录；用 getPhoneNumber 的 code 绑定手机号，若已有后台账号则合并")
    public Response bindPhone(@RequestBody WxBindPhoneRequest request, HttpServletRequest httpRequest) {
        return authService.bindPhone(request == null ? null : request.getCode(), httpRequest);
    }
}
