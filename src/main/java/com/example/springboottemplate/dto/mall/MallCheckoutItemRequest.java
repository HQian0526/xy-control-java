package com.example.springboottemplate.dto.mall;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "商城结算商品行")
public class MallCheckoutItemRequest {
    @ApiModelProperty(value = "商品业务id（productId）", required = true)
    private Long productId;

    @ApiModelProperty(value = "购买数量", required = true)
    private Integer quantity;
}
