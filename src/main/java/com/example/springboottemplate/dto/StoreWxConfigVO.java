package com.example.springboottemplate.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

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

    @ApiModelProperty("小程序类型 1商城类 2预约服务类")
    private Integer miniappType;
    @ApiModelProperty("是否展示库存 0否 1是")
    private Integer showStock;
    @ApiModelProperty("是否展示收货地址 0否 1是")
    private Integer showAddress;
    @ApiModelProperty("是否展示购买数量 0否 1是")
    private Integer showQuantity;
    @ApiModelProperty("是否展示购物车 0否 1是")
    private Integer showCart;
    @ApiModelProperty("下单按钮文字")
    private String orderBtnText;
    @ApiModelProperty("是否接入支付 0否 1是")
    private Integer enablePay;
    @ApiModelProperty("是否接入储值 0否 1是")
    private Integer enableRecharge;
    @ApiModelProperty("底部首页Tab文字")
    private String homeTabText;
    @ApiModelProperty("逛店列表按钮文字")
    private String listBtnText;
    @ApiModelProperty("是否展示销量 0否 1是")
    private Integer showSaleNum;
    @ApiModelProperty("是否展示配送费 0否 1是")
    private Integer showDeliveryFee;
    @ApiModelProperty("配送费（元）")
    private BigDecimal deliveryFee;
}
