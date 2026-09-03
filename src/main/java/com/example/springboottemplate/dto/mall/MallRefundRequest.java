package com.example.springboottemplate.dto.mall;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "商城订单退款请求（商户/管理员）")
public class MallRefundRequest {
    @ApiModelProperty(value = "商户订单号", required = true)
    private String orderNo;

    @ApiModelProperty(value = "是否全额退款；true 时忽略退款金额字段")
    private Boolean fullRefund;

    @ApiModelProperty(value = "部分退款金额（元）；fullRefund=false 时必填")
    private BigDecimal refundAmount;

    @ApiModelProperty(value = "部分退款金额（分）；与 refundAmount 二选一，优先用分")
    private Integer refundAmountFen;

    @ApiModelProperty(value = "退款原因（可选，会展示给用户）")
    private String reason;
}
