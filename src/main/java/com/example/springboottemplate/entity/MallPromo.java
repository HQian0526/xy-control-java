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

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("mall_promo")
@ApiModel(description = "店铺满减活动")
public class MallPromo {
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

    @ApiModelProperty(value = "活动名称")
    private String name;

    @ApiModelProperty(value = "1启用 0停用")
    private Integer status;

    @ApiModelProperty(value = "1永久有效 0指定过期时间")
    private Integer forever;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @ApiModelProperty(value = "过期时间")
    private Date endTime;

    @TableField(exist = false)
    @ApiModelProperty(value = "满减档位")
    private List<MallPromoTier> tiers;

    @TableField(exist = false)
    @ApiModelProperty(value = "档位文案")
    private String tierText;

    @TableField(exist = false)
    @ApiModelProperty(value = "有效期文案")
    private String expireText;

    @TableField(exist = false)
    @ApiModelProperty(value = "是否仍在有效期内")
    private Boolean effective;

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
