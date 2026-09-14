package com.example.springboottemplate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "微信小程序登录请求")
public class WxLoginRequest {
    @ApiModelProperty(value = "wx.login 获取的临时登录凭证 code", required = true)
    private String code;

    @ApiModelProperty(value = "当前小程序 AppID，买断包必传；空则走平台默认")
    private String appId;
}
