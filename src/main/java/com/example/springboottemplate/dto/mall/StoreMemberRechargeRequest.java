package com.example.springboottemplate.dto.mall;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "会员储值请求")
public class StoreMemberRechargeRequest {
    @ApiModelProperty(value = "店铺id（未传则用当前逛店/绑定店）")
    private Long storeId;

    @ApiModelProperty(value = "储值金额（元），每次至少 100")
    private BigDecimal amount;
}
