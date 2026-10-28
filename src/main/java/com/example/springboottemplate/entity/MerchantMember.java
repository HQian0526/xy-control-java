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

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("merchant_member")
@ApiModel(description = "商家连锁会员（共享余额）")
public class MerchantMember {
    @TableId(type = IdType.INPUT)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "商家账号id")
    private Long merchantUserId;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "顾客用户id")
    private Long userId;

    @ApiModelProperty(value = "连锁储值会员 0否 1是")
    private Integer isMember;

    @ApiModelProperty(value = "共享余额（元）")
    private BigDecimal balance;

    private Date createdTime;
    private Date updateTime;

    @TableLogic
    private Integer deleted;
}
