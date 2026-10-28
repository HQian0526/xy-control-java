package com.example.springboottemplate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("store_verify_log")
@ApiModel(description = "店铺核验记录")
public class StoreVerifyLog {
    @TableId(type = IdType.INPUT)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键id")
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "店铺id")
    private Long storeId;

    @TableField(exist = false)
    @ApiModelProperty(value = "所属店铺名")
    private String storeName;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "顾客用户id")
    private Long customerUserId;

    @ApiModelProperty(value = "顾客手机号")
    private String customerPhone;

    @ApiModelProperty(value = "顾客昵称")
    private String customerName;

    @ApiModelProperty(value = "核验金额（元）")
    private BigDecimal amount;

    @ApiModelProperty(value = "备注")
    private String remark;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "操作人用户id")
    private Long operatorUserId;

    @ApiModelProperty(value = "操作人类型 1商家 2员工 3管理员")
    private Integer operatorType;

    @ApiModelProperty(value = "操作人姓名")
    private String operatorName;

    @TableField(exist = false)
    @ApiModelProperty(value = "操作人类型文案")
    private String operatorTypeName;

    private String createdBy;
    private Date createdTime;

    @TableField(exist = false)
    private String createdTimeStart;

    @TableField(exist = false)
    private String createdTimeEnd;
}
