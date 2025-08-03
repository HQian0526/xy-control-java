package com.example.springboottemplate.dto.contract;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ContractDTO {
    // 基础信息
    private Long id;
    private String contractName;
    private Integer contractType; // 1:租赁合同 2:销售合同...
    private Long lessorId;       // 出租方ID
    private Long lesseeId;      // 承租方ID
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalAmount;
    private Integer paymentCycle; // 1:月付 2:季付 3:年付
    private String remark;
    private List<ContractItemDTO> items;
    // 签署人信息（如果需要前端指定签署人）
    private List<SignerDTO> signers;
}
