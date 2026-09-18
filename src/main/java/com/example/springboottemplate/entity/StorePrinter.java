package com.example.springboottemplate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("store_printer")
@ApiModel(description = "店铺飞鹅云打印机")
public class StorePrinter {
    @TableId(type = IdType.INPUT)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty("店铺id")
    private Long storeId;

    @ApiModelProperty("打印机编号 SN")
    private String printerSn;

    @ApiModelProperty("打印机识别码 Key")
    private String printerKey;

    @ApiModelProperty("备注名")
    private String printerName;

    @ApiModelProperty("1自动打印 0关闭")
    private Integer printEnabled;

    @ApiModelProperty("1语音播报 0关闭")
    private Integer voiceEnabled;

    @ApiModelProperty("打印联数")
    private Integer copies;

    private String lastStatus;
    private String lastBindMsg;
    private Date createdTime;
    private String createdBy;
    private Date updateTime;
    private String updateBy;

    @TableLogic
    private Integer deleted;
}
