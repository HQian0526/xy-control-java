package com.example.springboottemplate.dto.contract;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ContractItemDTO {
    private Long id;
    private String itemName;
    private String itemType;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String specification;
    private String remark;
}
