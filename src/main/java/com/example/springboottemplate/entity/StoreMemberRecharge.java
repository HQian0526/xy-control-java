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
@TableName("store_member_recharge")
@ApiModel(description = "店铺会员储值单")
public class StoreMemberRecharge {
    @TableId(type = IdType.INPUT)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键id")
    private Long id;

    @ApiModelProperty(value = "充值单号")
    private String rechargeNo;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "店铺id")
    private Long storeId;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "付款人openid")
    private String openid;

    @ApiModelProperty(value = "下单小程序 AppID")
    private String appId;

    @ApiModelProperty(value = "收款微信商户号")
    private String mchId;

    @ApiModelProperty(value = "充值金额（元）")
    private BigDecimal amount;

    @ApiModelProperty(value = "充值金额（分）")
    private Integer amountFen;

    /** 0待支付 1已支付 2已关闭 */
    @ApiModelProperty(value = "支付状态")
    private Integer payStatus;

    @ApiModelProperty(value = "微信预支付id")
    private String prepayId;

    @ApiModelProperty(value = "微信支付单号")
    private String transactionId;

    @ApiModelProperty(value = "支付成功时间")
    private Date paidTime;

    /** 0待结算 1已结算 */
    @ApiModelProperty(value = "结算状态")
    private Integer settleStatus;

    @ApiModelProperty(value = "标记已结算时间")
    private Date settleTime;

    @ApiModelProperty(value = "结算操作人")
    private String settleBy;

    @ApiModelProperty(value = "创建人")
    private String createdBy;

    @ApiModelProperty(value = "创建时间")
    private Date createdTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @TableLogic
    @ApiModelProperty(value = "逻辑删除")
    private Integer deleted;
}
