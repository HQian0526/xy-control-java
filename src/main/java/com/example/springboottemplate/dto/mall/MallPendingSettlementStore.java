package com.example.springboottemplate.dto.mall;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "待结算店铺")
public class MallPendingSettlementStore {
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "店铺id")
    private Long storeId;

    @ApiModelProperty(value = "店铺名称")
    private String storeName;

    @ApiModelProperty(value = "待结算订单数")
    private Integer orderCount;

    @ApiModelProperty(value = "待结算金额（元）")
    private BigDecimal pendingAmount;

    @ApiModelProperty(value = "最近一笔支付时间")
    private String lastPaidTime;

    @ApiModelProperty(value = "上次结算时间")
    private String lastSettleTime;

    @ApiModelProperty(value = "结算状态文案")
    private String status;
}
