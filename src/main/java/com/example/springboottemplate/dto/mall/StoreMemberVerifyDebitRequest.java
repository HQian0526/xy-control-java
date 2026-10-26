package com.example.springboottemplate.dto.mall;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "商家核验扣减本店顾客储值")
public class StoreMemberVerifyDebitRequest {
    @ApiModelProperty(value = "顾客手机号", required = true)
    private String phone;

    @ApiModelProperty(value = "核验金额（元）", required = true)
    private BigDecimal amount;

    @ApiModelProperty(value = "店铺id（管理员必填；商户可不传，取本店）")
    private Long storeId;
}
