package com.example.springboottemplate.entity.contract;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor // 生成全参构造函数
@NoArgsConstructor // 生成无参构造函数
@ApiModel(description = "付款记录")
public class PayRecord {
    @ApiModelProperty(value = "id", required = true)
    private Long id;

    @ApiModelProperty(value = "合同id", required = true)
    private Long contractId;

    @ApiModelProperty(value = "付款单号", required = false)
    private String paymentNo;

    @ApiModelProperty(value = "付款金额", required = false)
    private BigDecimal paymentAmount;

    @ApiModelProperty(value = "付款日期", required = false)
    private LocalDate paymentDate;

    @ApiModelProperty(value = "支付方式", required = false)
    private Integer paymentMethod;

    @ApiModelProperty(value = "状态", required = false)
    private Integer payStatus;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

    @ApiModelProperty(value = "创建时间", required = false)
    private LocalDateTime createdTime;
}
