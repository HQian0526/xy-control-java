package com.example.springboottemplate.dto.mall;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "商城下单并调起支付返回")
public class MallPayPrepareVO {
    @ApiModelProperty(value = "是否 mock（true 时前端勿调 requestPayment，改调 mockConfirmPay）")
    private boolean mock;

    @ApiModelProperty(value = "商户订单号")
    private String orderNo;

    @ApiModelProperty(value = "应付金额（元）")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "支付状态 0待支付 1已支付 2已关闭")
    private Integer payStatus;

    @ApiModelProperty(value = "小程序 appId")
    private String appId;

    @ApiModelProperty(value = "时间戳")
    private String timeStamp;

    @ApiModelProperty(value = "随机串")
    private String nonceStr;

    @ApiModelProperty(value = "订单详情扩展字符串，格式 prepay_id=xxx")
    private String packageValue;

    @ApiModelProperty(value = "签名方式")
    private String signType;

    @ApiModelProperty(value = "支付签名")
    private String paySign;
}
