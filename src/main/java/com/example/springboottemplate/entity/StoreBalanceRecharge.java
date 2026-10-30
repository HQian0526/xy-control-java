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

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("store_balance_recharge")
@ApiModel(description = "店铺余额储值单")
public class StoreBalanceRecharge {
    @TableId(type = IdType.INPUT)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @ApiModelProperty(value = "储值单号")
    private String rechargeNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long storeId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private String openid;
    private String appId;
    private String mchId;
    private BigDecimal amount;
    private Integer amountFen;
    private Integer payStatus;
    private String prepayId;
    private String transactionId;
    private Date paidTime;
    private Integer settleStatus;
    private Date settleTime;
    private String settleBy;
    private Integer wxShippingStatus;
    private Integer wxShippingErrcode;
    private String wxShippingErrmsg;
    private Integer wxShippingRetry;
    private Date wxShippingTime;
    private String createdBy;
    private Date createdTime;
    private Date updateTime;

    @TableLogic
    private Integer deleted;
}
