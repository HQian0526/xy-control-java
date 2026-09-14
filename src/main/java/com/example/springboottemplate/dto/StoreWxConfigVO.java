package com.example.springboottemplate.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "店铺小程序/支付配置（不含密钥明文）")
public class StoreWxConfigVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long storeId;
    private String storeName;
    private String appId;
    private String mchId;
    private String merchantSerialNumber;
    private String publicKeyId;
    private String notifyUrl;
    private String refundNotifyUrl;
    private Integer enabled;

    @ApiModelProperty("是否已配置 AppSecret")
    private boolean hasAppSecret;
    @ApiModelProperty("是否已配置 APIv3 密钥")
    private boolean hasApiV3Key;
    @ApiModelProperty("是否已配置商户私钥")
    private boolean hasPrivateKey;
    @ApiModelProperty("是否已配置微信支付公钥")
    private boolean hasPublicKey;

    @ApiModelProperty("小程序登录材料是否齐全")
    private boolean miniappReady;
    @ApiModelProperty("微信支付材料是否齐全")
    private boolean payReady;
    @ApiModelProperty("未配店铺配置时走平台试用代收")
    private boolean platformFallback;
}
