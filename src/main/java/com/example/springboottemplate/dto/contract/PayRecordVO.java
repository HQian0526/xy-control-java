package com.example.springboottemplate.dto.contract;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PayRecordVO {
    private Long id;
    private String paymentNo;      // 付款单号
    private BigDecimal paymentAmount;
    private LocalDate paymentDate;
    private Integer paymentMethod; // 付款方式（1:现金, 2:转账...）
    private Integer status;        // 状态（1:待支付, 2:已支付...）
    private String remark;
    private LocalDateTime createTime;
}
