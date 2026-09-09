package com.example.springboottemplate.dto.mall;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "店铺订单金额流水")
public class MallIncomeFlowVO {
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "店铺业务id")
    private Long storeId;

    @ApiModelProperty(value = "店铺名称")
    private String storeName;

    @ApiModelProperty(value = "统计维度 year/quarter/month/day")
    private String periodType;

    @ApiModelProperty(value = "合计")
    private MallIncomeFlowRow summary;

    @ApiModelProperty(value = "分账期明细（含无单账期，金额为0）")
    private List<MallIncomeFlowRow> list;
}
