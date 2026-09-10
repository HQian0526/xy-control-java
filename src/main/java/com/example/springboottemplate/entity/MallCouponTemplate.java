package com.example.springboottemplate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("mall_coupon_template")
@ApiModel(description = "店铺优惠券模板")
public class MallCouponTemplate {
    @TableId(type = IdType.INPUT)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键id")
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "店铺id")
    private Long storeId;

    @TableField(exist = false)
    @ApiModelProperty(value = "店铺名")
    private String storeName;

    @ApiModelProperty(value = "优惠券名称")
    private String name;

    @ApiModelProperty(value = "使用门槛（元），0为无门槛")
    private BigDecimal thresholdAmount;

    @ApiModelProperty(value = "减免金额（元）")
    private BigDecimal discountAmount;

    @ApiModelProperty(value = "发放总量，空表示不限")
    private Integer totalCount;

    @ApiModelProperty(value = "已领取数量")
    private Integer issuedCount;

    @ApiModelProperty(value = "每人限领")
    private Integer perUserLimit;

    @ApiModelProperty(value = "1顾客领取 2商家发放")
    private Integer issueType;

    @TableField(exist = false)
    @ApiModelProperty(value = "直接发放的用户id")
    private List<Object> userIds;

    @ApiModelProperty(value = "1领取中 0停领")
    private Integer status;

    @ApiModelProperty(value = "1永久有效 0指定过期时间")
    private Integer forever;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @ApiModelProperty(value = "过期时间")
    private Date endTime;

    @TableField(exist = false)
    @ApiModelProperty(value = "有效期文案")
    private String expireText;

    @TableField(exist = false)
    @ApiModelProperty(value = "是否仍在有效期内")
    private Boolean effective;

    @TableField(exist = false)
    @ApiModelProperty(value = "当前用户是否已领取")
    private Boolean claimed;

    @TableField(exist = false)
    @ApiModelProperty(value = "剩余可领数量，空表示不限")
    private Integer remainCount;

    @ApiModelProperty(value = "创建人")
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @ApiModelProperty(value = "创建时间")
    private Date createdTime;

    @ApiModelProperty(value = "更新人")
    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @TableLogic
    @ApiModelProperty(value = "逻辑删除")
    private Integer deleted;
}
