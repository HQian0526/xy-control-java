package com.example.springboottemplate.dto.mall;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "商家代顾客储值")
public class StoreMemberAssistRechargeRequest {
    @ApiModelProperty(value = "顾客用户id", required = true)
    private Long userId;

    @ApiModelProperty(value = "储值金额（元）", required = true)
    private BigDecimal amount;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "店铺id（管理员必填；商户可不传，取本店）")
    private Long storeId;
}
