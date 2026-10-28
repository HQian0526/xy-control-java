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
@TableName("store_staff")
@ApiModel(description = "店铺员工")
public class StoreStaff {
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
    @ApiModelProperty(value = "员工用户id")
    private Long userId;

    @TableField(exist = false)
    @ApiModelProperty(value = "员工手机号（新增时必填）")
    private String phone;

    @TableField(exist = false)
    @ApiModelProperty(value = "员工昵称")
    private String realName;

    @TableField(exist = false)
    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "状态 1启用 0停用")
    private Integer status;

    @ApiModelProperty(value = "备注")
    private String remark;

    @TableField(exist = false)
    @ApiModelProperty(value = "手机号或昵称模糊查询")
    private String keyword;

    private String createdBy;
    private Date createdTime;
    private String updateBy;
    private Date updateTime;

    @TableLogic
    private Integer deleted;
}
