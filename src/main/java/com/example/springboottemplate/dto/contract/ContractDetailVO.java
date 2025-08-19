package com.example.springboottemplate.dto.contract;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContractDetailVO {
    // 合同基本信息
    private Long id;
    private String contractNo;
    private String contractName;
    private Integer contractType;
    private String lessorName;      // 出租方名称
    private String lesseeName;      // 承租方名称
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalAmount;
    private Integer paymentCycle;   // 付款周期
    private Integer contractStatus;
    private LocalDate signDate;
    private String remark;
    private LocalDateTime createTime;

    // 合同明细项
    private List<ContractItemVO> items;

    // 付款记录
    private List<PayRecordVO> paymentRecords;

    // 签署信息
    private ContractSignatureVO signatureInfo;
}
