package com.example.springboottemplate.dto.mall;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
@ApiModel(description = "商家按手机号查询的本店顾客储值信息")
public class StoreMemberCustomerVO {
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "顾客用户id")
    private Long userId;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "店铺id")
    private Long storeId;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "是否本店会员")
    private boolean member;

    @ApiModelProperty(value = "本店会员余额（元）")
    private BigDecimal balance;
}
