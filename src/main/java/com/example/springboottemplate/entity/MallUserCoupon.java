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

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("mall_user_coupon")
@ApiModel(description = "用户持有优惠券")
public class MallUserCoupon {
    @TableId(type = IdType.INPUT)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键id")
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "券模板id")
    private Long templateId;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "店铺id")
    private Long storeId;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "券名称快照")
    private String name;

    @ApiModelProperty(value = "门槛快照（元）")
    private BigDecimal thresholdAmount;

    @ApiModelProperty(value = "面额快照（元）")
    private BigDecimal discountAmount;

    /** 0未使用 1已锁定 2已使用 3已过期 4已作废 */
    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "1永久 0有过期时间")
    private Integer forever;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @ApiModelProperty(value = "过期时间")
    private Date expireTime;

    @ApiModelProperty(value = "锁定/核销订单号")
    private String orderNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date receiveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date lockTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date useTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String storeName;

    @TableField(exist = false)
    private String expireText;

    @TableField(exist = false)
    private Boolean usable;

    @TableField(exist = false)
    private String disableReason;
}
