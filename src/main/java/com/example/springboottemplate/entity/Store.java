package com.example.springboottemplate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@AllArgsConstructor // 生成全参构造函数
@NoArgsConstructor // 生成无参构造函数
@ApiModel(description = "商户信息")
public class Store {
    @TableId(type = IdType.INPUT)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键id（雪花算法，新增时无需传入）", required = false)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "商户业务id（雪花算法，新增时无需传入）", required = false)
    private Long storeId;

    @ApiModelProperty(value = "商户名", required = true)
    private String storeName;

    @ApiModelProperty(value = "店铺头像", required = false)
    private String avatar;

    @ApiModelProperty(value = "商户地址", required = true)
    private String address;

    @ApiModelProperty(value = "关联的用户Id", required = true)
    private Long userId;

    @ApiModelProperty(value = "商户类型 1永久 2租用", required = true)
    private Integer storeType;

    @ApiModelProperty(value = "商户状态 1营业中 2打烊", required = false)
    private Integer storeStatus;

    @ApiModelProperty(value = "营业时间JSON", required = false)
    private String businessHours;

    @ApiModelProperty(value = "临时打烊截止时间", required = false)
    private Date closedUntil;

    @ApiModelProperty(value = "手动开始营业截止时间", required = false)
    private Date openUntil;

    @ApiModelProperty(value = "配送费（元）", required = false)
    private BigDecimal deliveryFee;

    @TableField(exist = false)
    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private Boolean closedUntilCleared;

    @TableField(exist = false)
    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private Boolean openUntilCleared;

    @TableField(exist = false)
    @ApiModelProperty(value = "当前是否可下单（计算字段）", required = false)
    private Boolean acceptingOrders;

    @TableField(exist = false)
    @ApiModelProperty(value = "当前是否处于手动打烊（计算字段）", required = false)
    private Boolean manuallyClosed;

    @TableField(exist = false)
    @ApiModelProperty(value = "open/rest/closed（计算字段）", required = false)
    private String openStatus;

    @TableField(exist = false)
    @ApiModelProperty(value = "营业时间展示文案（计算字段）", required = false)
    private String businessHoursText;

    @TableField(exist = false)
    @ApiModelProperty(value = "状态说明（计算字段）", required = false)
    private String statusHint;

    @TableField(exist = false)
    @ApiModelProperty(value = "下次开门时刻文案（计算字段）", required = false)
    private String nextOpenText;

    @TableField(exist = false)
    @ApiModelProperty(value = "下次休息时刻文案（计算字段）", required = false)
    private String nextCloseText;

    @TableField(exist = false)
    @ApiModelProperty(value = "临时打烊截止文案（计算字段）", required = false)
    private String closedUntilText;

    @TableField(exist = false)
    @ApiModelProperty(value = "手动营业截止文案（计算字段）", required = false)
    private String openUntilText;

    @ApiModelProperty(value = "商户到期时间", required = false)
    private String storeTime;

    @ApiModelProperty(value = "法人姓名", required = true)
    private String identityName;

    @ApiModelProperty(value = "工商主体名称", required = true)
    private String identityCompanyName;

    @ApiModelProperty(value = "营业执照", required = false)
    private String businessLicense;

    @ApiModelProperty(value = "法人身份证号", required = true)
    private String identityNo;

    @ApiModelProperty(value = "法人身份证照片", required = false)
    private String identityPhoto;

    @ApiModelProperty(value = "法人手机号", required = false)
    private String identityPhone;

    @ApiModelProperty(value = "法人邮箱", required = false)
    private String identityEmail;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

    @ApiModelProperty(value = "店铺创建时间", required = false)
    private Date createdTime;

    @ApiModelProperty(value = "店铺信息创建人", required = false)
    private String createdBy;

    @ApiModelProperty(value = "信息更新时间", required = false)
    private Date updateTime;

    @ApiModelProperty(value = "店铺信息更新人", required = false)
    private String updateBy;

    @TableField(exist = false) // 表示该字段不在数据库中
    @ApiModelProperty(value = "用户名", required = false)
    private String userName;

    @TableField(exist = false) // 表示该字段不在数据库中
    @ApiModelProperty(value = "真实姓名", required = false)
    private String realName;

    @ApiModelProperty(value = "逻辑删除标识", required = false)
    @TableLogic
    private Integer deleted;
}
