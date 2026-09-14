package com.example.springboottemplate.dto.mall;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "管理员标记店铺已结算")
public class MallSettleStoreRequest {
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "店铺id", required = true)
    private Long storeId;
}
