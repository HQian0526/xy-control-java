package com.example.springboottemplate.dto.mall;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "店铺订单金额流水账期行")
public class MallIncomeFlowRow {
    @ApiModelProperty(value = "账期编码，如 2026 / 2026-Q1 / 2026-03 / 2026-03-15")
    private String period;

    @ApiModelProperty(value = "账期展示文案")
    private String periodLabel;

    @ApiModelProperty(value = "已支付订单数")
    private Integer orderCount;

    @ApiModelProperty(value = "实收金额（元）")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "累计退款（元），挂在原支付账期")
    private BigDecimal refundAmount;

    @ApiModelProperty(value = "净额（元）= 实收 - 退款")
    private BigDecimal netAmount;
}
