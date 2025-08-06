package com.example.springboottemplate.entity.contract;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor // 生成全参构造函数
@NoArgsConstructor // 生成无参构造函数
@ApiModel(description = "合同细则")
public class ContractItem {
    @ApiModelProperty(value = "id", required = true)
    private Long id;

    @ApiModelProperty(value = "合同id", required = true)
    private Long contractId;

    @ApiModelProperty(value = "合同项名称", required = false)
    private String itemName;

    @ApiModelProperty(value = "合同项类型", required = false)
    private Integer itemType;

    @ApiModelProperty(value = "数量", required = false)
    private Integer quantity;

    @ApiModelProperty(value = "单价", required = false)
    private BigDecimal unitPrice;

    @ApiModelProperty(value = "总价", required = false)
    private BigDecimal totalPrice;

    @ApiModelProperty(value = "规格", required = false)
    private String specification;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;
}
