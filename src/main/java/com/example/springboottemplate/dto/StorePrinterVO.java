package com.example.springboottemplate.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "店铺飞鹅打印机")
public class StorePrinterVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long storeId;
    private String storeName;
    private String printerSn;
    private String printerKey;
    private String printerName;
    private Integer printEnabled;
    private Integer voiceEnabled;
    private Integer copies;
    private String lastStatus;
    private String lastBindMsg;

    @ApiModelProperty("是否已填写 SN")
    private boolean bound;
    @ApiModelProperty("平台 yml 是否已配飞鹅 ukey")
    private boolean platformReady;
}
