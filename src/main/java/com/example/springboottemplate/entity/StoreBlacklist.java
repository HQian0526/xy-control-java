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

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("store_blacklist")
@ApiModel(description = "店铺黑名单")
public class StoreBlacklist {
    @TableId(type = IdType.INPUT)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键id（雪花，新增可不传）")
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "拉黑归属店铺id（商户可不传，系统取本店）")
    private Long storeId;

    @TableField(exist = false)
    @ApiModelProperty(value = "店铺名")
    private String storeName;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "系统用户id（有则按用户拦截；外部人员可不传）")
    private Long userId;

    @ApiModelProperty(value = "拉黑手机号（外部人员必填；系统用户可空）")
    private String phone;

    @ApiModelProperty(value = "姓名（展示用）")
    private String realName;

    @ApiModelProperty(value = "拉黑原因")
    private String reason;

    @ApiModelProperty(value = "备注")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @ApiModelProperty(value = "创建时间")
    private Date createdTime;

    @ApiModelProperty(value = "创建人")
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "更新人")
    private String updateBy;

    @TableLogic
    @ApiModelProperty(value = "逻辑删除 0正常 1解除拉黑")
    private Integer deleted;
}
