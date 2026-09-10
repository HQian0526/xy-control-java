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

    @ApiModelProperty(value = "选券失败原因")
    private String couponError;

    @ApiModelProperty(value = "本店可用/不可用券")
    private List<MallUserCoupon> coupons;
}
