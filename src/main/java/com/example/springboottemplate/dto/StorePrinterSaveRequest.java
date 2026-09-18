package com.example.springboottemplate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "保存店铺飞鹅打印机；printerKey 留空表示不修改")
public class StorePrinterSaveRequest {
    @ApiModelProperty(value = "店铺id", required = true)
    private Long storeId;
    private String printerSn;
    @ApiModelProperty("留空不改")
    private String printerKey;
    private String printerName;
    private Integer printEnabled;
    private Integer voiceEnabled;
    private Integer copies;
}
