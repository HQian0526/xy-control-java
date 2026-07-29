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
@TableName("product_catagory")
@ApiModel(description = "商品分类信息")
public class Catagory {
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "id", required = true)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "分类id（后端自动生成）", required = false)
    private Long catagoryId;

    @ApiModelProperty(value = "分类名称", required = false)
    private String catagoryName;

    @ApiModelProperty(value = "所属商户", required = false)
    private String storeId;

    @TableField(exist = false)
    @ApiModelProperty(value = "所属商户名", required = false)
    private String storeName;

    @ApiModelProperty(value = "分类状态", required = false)
    private Integer catagoryStatus;

    @ApiModelProperty(value = "排序号", required = false)
    private Integer orderNum;

    @ApiModelProperty(value = "创建人", required = false)
    private String createdBy;

    @ApiModelProperty(value = "创建时间", required = false)
    private Date createdTime;

    @ApiModelProperty(value = "更新人", required = false)
    private String updateBy;

    @ApiModelProperty(value = "更新时间", required = false)
    private Date updateTime;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

    @ApiModelProperty(value = "逻辑删除标识", required = false)
    @TableLogic
    private Integer deleted;
}
