package com.example.springboottemplate.dto.mall;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "商家修改本店顾客会员身份")
public class StoreMemberStatusRequest {
    @ApiModelProperty(value = "顾客用户id", required = true)
    private Long userId;

    @ApiModelProperty(value = "店铺id（管理员必填；商户可不传，取本店）")
    private Long storeId;

    @ApiModelProperty(value = "是否本店会员 0基础用户 1会员", required = true)
    private Integer isMember;
}
