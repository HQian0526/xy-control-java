package com.example.springboottemplate.dto.contract;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ContractItemVO {
    private Long id;
    private String itemName;       // 物品名称
    private Integer itemType;       // 物品类型
    private Integer quantity;      // 数量
    private BigDecimal unitPrice;  // 单价
    private BigDecimal totalPrice; // 总价
    private String specification;  // 规格
    private String remark;
}
