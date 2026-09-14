package com.example.springboottemplate.dto.mall;

import com.example.springboottemplate.entity.MallUserCoupon;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "结算预览")
public class MallCheckoutPreviewVO {
    @ApiModelProperty(value = "商品金额")
    private BigDecimal goodsAmount;

    @ApiModelProperty(value = "配送费")
    private BigDecimal deliveryFee;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "满减活动id")
    private Long promoId;

    @ApiModelProperty(value = "满减金额")
    private BigDecimal promoDiscount;

    @ApiModelProperty(value = "满减文案")
    private String promoText;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "选用的用户券id")
    private Long userCouponId;

    @ApiModelProperty(value = "优惠券减免")
    private BigDecimal couponDiscount;

    @ApiModelProperty(value = "优惠合计")
    private BigDecimal discountAmount;

    @ApiModelProperty(value = "优惠说明")
    private String discountDesc;

    @ApiModelProperty(value = "应付金额")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "支付渠道 1微信 2会员余额")
    private Integer payChannel;

    @ApiModelProperty(value = "本单是否按会员价")
    private Boolean memberPriced;

    @ApiModelProperty(value = "是否本店会员")
    private Boolean member;

    @ApiModelProperty(value = "本店会员余额")
    private BigDecimal memberBalance;

    @ApiModelProperty(value = "按会员价应付（余额不足时仍返回，便于提示）")
    private BigDecimal memberPayAmount;

    @ApiModelProperty(value = "会员余额不足，本单按原价微信")
    private Boolean memberPriceUnavailable;

    @ApiModelProperty(value = "选券失败原因")
    private String couponError;

    @ApiModelProperty(value = "本店可用/不可用券")
    private List<MallUserCoupon> coupons;
}
