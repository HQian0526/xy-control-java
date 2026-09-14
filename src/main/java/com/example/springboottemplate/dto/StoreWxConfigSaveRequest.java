package com.example.springboottemplate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "保存店铺小程序/支付配置；密钥字段留空表示不修改")
public class StoreWxConfigSaveRequest {
    @ApiModelProperty(value = "店铺id", required = true)
    private Long storeId;
    private String appId;
    @ApiModelProperty("留空不改")
    private String appSecret;
    private String mchId;
    @ApiModelProperty("留空不改")
    private String apiV3Key;
    private String merchantSerialNumber;
    @ApiModelProperty("留空不改")
    private String privateKey;
    @ApiModelProperty("留空不改")
    private String publicKey;
    private String publicKeyId;
    private String notifyUrl;
    private String refundNotifyUrl;
    private Integer enabled;
}
