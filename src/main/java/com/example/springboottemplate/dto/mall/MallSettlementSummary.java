package com.example.springboottemplate.dto.mall;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "商家待结算与今日收入")
public class MallSettlementSummary {
    @ApiModelProperty(value = "店铺余额，等于平台代收的待结算金额（元）")
    private BigDecimal pendingAmount;

    @ApiModelProperty(value = "本月销售额：本月微信支付商品净额 + 本月会员储值（元）")
    private BigDecimal monthSales;

    @ApiModelProperty(value = "今日收入：今日微信支付商品净额 + 今日会员储值（元）")
    private BigDecimal todayIncome;
}
