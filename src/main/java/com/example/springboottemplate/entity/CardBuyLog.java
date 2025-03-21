package com.example.springboottemplate.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor // 生成全参构造函数
@NoArgsConstructor // 生成无参构造函数
@ApiModel(description = "套餐购买记录")
public class CardBuyLog {
    @ApiModelProperty(value = "id", required = false)
    private int id;

    @ApiModelProperty(value = "购买用户id", required = true)
    private int userId;

    @ApiModelProperty(value = "购买套餐id", required = true)
    private int cardId;

    @ApiModelProperty(value = "套餐购买时间", required = false)
    private String createdTime;

    @ApiModelProperty(value = "订单状态 1已付款 2已退款 3退款中", required = false)
    private int status;

    @ApiModelProperty(value = "是否已开票 1否 2是", required = false)
    private int ticketStatus;
}
