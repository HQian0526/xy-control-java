package com.example.springboottemplate.controller.system;

import com.example.springboottemplate.annotation.OperLog;
import com.example.springboottemplate.annotation.OperTypes;
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
    @OperLog(module = "登录认证", type = OperTypes.QUERY, remark = "微信小程序登录")
    public Response login(@RequestBody WxLoginRequest request) {
        try {
            return authService.wxLogin(
                    request == null ? null : request.getCode(),
                    request == null ? null : request.getAppId());
        } catch (Exception e) {
            return wxFail(e);
        }
    }

    @PostMapping("/bindPhone")
    @ResponseBody
    @ApiOperation(value = "绑定微信手机号", notes = "需登录；用 getPhoneNumber 的 code 绑定手机号，若已有后台账号则合并并换绑微信")
    @OperLog(module = "微信用户", type = OperTypes.UPDATE)
    public Response bindPhone(@RequestBody WxBindPhoneRequest request, HttpServletRequest httpRequest) {
        try {
            return authService.bindPhone(request == null ? null : request.getCode(), httpRequest);
        } catch (Exception e) {
            return wxFail(e);
        }
    }

    /** 业务失败返回 400，避免未捕获异常变成 HTTP 500 Internal Server Error */
    private Response wxFail(Exception e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            message = "操作失败，请稍后重试";
        }
        return Response.fail(400, message);
    }
}
