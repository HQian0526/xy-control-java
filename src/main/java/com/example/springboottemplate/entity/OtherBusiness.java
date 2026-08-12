package com.example.springboottemplate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("other_business")
@ApiModel(description = "其他业务信息")
public class OtherBusiness {
    @TableId(type = IdType.INPUT)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键id（雪花算法）", required = true)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "所属店铺id", required = false)
    private Long storeId;

    @TableField(exist = false)
    @ApiModelProperty(value = "所属店铺名", required = false)
    private String storeName;

    @ApiModelProperty(value = "业务名称", required = false)
    private String businessName;

    @ApiModelProperty(value = "业务描述", required = false)
    private String businessDesc;

    @ApiModelProperty(value = "业务收费标准", required = false)
    private String businessFee;

    @ApiModelProperty(value = "创建人", required = false)
    private String createdBy;

    @ApiModelProperty(value = "创建时间", required = false)
    private Date createdTime;

    @ApiModelProperty(value = "更新人", required = false)
    private String updateBy;

    @ApiModelProperty(value = "更新时间", required = false)
    private Date updateTime;

    @ApiModelProperty(value = "逻辑删除标识", required = false)
    @TableLogic
    private Integer deleted;
}
