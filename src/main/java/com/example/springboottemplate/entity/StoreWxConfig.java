package com.example.springboottemplate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("store_wx_config")
@ApiModel(description = "店铺小程序与微信支付配置")
public class StoreWxConfig {
    @TableId(type = IdType.INPUT)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "店铺id")
    private Long storeId;

    private String appId;
    private String appSecret;
    private String mchId;
    private String apiV3Key;
    private String merchantSerialNumber;
    private String privateKey;
    private String publicKey;
    private String publicKeyId;
    private String notifyUrl;
    private String refundNotifyUrl;
    private Integer enabled;
    private Date createdTime;
    private String createdBy;
    private Date updateTime;
    private String updateBy;

    @TableLogic
    private Integer deleted;
}
