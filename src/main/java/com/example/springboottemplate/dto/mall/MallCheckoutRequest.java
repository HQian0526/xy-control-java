package com.example.springboottemplate.dto.mall;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "商城结算下单请求")
public class MallCheckoutRequest {
    @ApiModelProperty(value = "联系电话", required = true)
    private String contact;

    @ApiModelProperty(value = "收货地址", required = true)
    private String address;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "购物车商品", required = true)
    private List<MallCheckoutItemRequest> items;

    @ApiModelProperty(value = "选用的用户优惠券id，可不传")
    private Long userCouponId;
}
