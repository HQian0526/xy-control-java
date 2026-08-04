package com.example.springboottemplate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "微信小程序绑定手机号请求")
public class WxBindPhoneRequest {
    @ApiModelProperty(value = "button open-type=getPhoneNumber 返回的 code", required = true)
    private String code;
}
