package com.example.springboottemplate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "保存店铺小程序权限配置")
public class StoreMiniappUiSaveRequest {
    @ApiModelProperty(value = "店铺id", required = true)
    private Long storeId;

    @ApiModelProperty(value = "小程序类型 1商城类 2预约服务类", required = true)
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

    @ApiModelProperty("是否接入储值 0否 1是（未接入支付时强制关闭）")
    private Integer enableRecharge;

    @ApiModelProperty("底部首页Tab文字")
    private String homeTabText;

    @ApiModelProperty("逛店列表按钮文字")
    private String listBtnText;

    @ApiModelProperty("是否展示销量 0否 1是")
    private Integer showSaleNum;

    @ApiModelProperty("是否展示配送费 0否 1是")
    private Integer showDeliveryFee;

    @ApiModelProperty("配送费（元），展示配送费时生效")
    private BigDecimal deliveryFee;
}
