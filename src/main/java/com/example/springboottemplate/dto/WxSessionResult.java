package com.example.springboottemplate.dto;

import lombok.Data;

/**
 * 微信 jscode2session 返回结果
 */
@Data
public class WxSessionResult {
    private String openid;
    private String sessionKey;
    private String unionid;
    private Integer errcode;
    private String errmsg;
}
