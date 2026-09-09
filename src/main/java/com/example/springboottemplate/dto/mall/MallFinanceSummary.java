package com.example.springboottemplate.dto.mall;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "店铺资金流水合计")
public class MallFinanceSummary {
    @ApiModelProperty(value = "累计收入（已支付订单实收）")
    private BigDecimal income;

    @ApiModelProperty(value = "累计支出（已退款合计）")
    private BigDecimal expense;
}
