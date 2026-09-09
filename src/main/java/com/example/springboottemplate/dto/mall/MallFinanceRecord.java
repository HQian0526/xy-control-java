package com.example.springboottemplate.dto.mall;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "店铺资金流水明细（一单一收入 / 一单一退款）")
public class MallFinanceRecord {
    @ApiModelProperty(value = "流水id")
    private String id;

    @ApiModelProperty(value = "income 收入 / expense 退款")
    private String type;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "关联订单号")
    private String orderNo;

    @ApiModelProperty(value = "金额（元）")
    private BigDecimal amount;

    @ApiModelProperty(value = "发生时间 yyyy-MM-dd HH:mm:ss")
    private String time;

    @ApiModelProperty(value = "状态文案")
    private String status;

    @ApiModelProperty(value = "店铺名称")
    private String storeName;
}
