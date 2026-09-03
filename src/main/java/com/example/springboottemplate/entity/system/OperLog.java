package com.example.springboottemplate.entity.system;

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
@TableName("oper_log")
@ApiModel(description = "操作日志")
public class OperLog {
    @TableId(type = IdType.INPUT)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "日志id（雪花，新增可不传）")
    private Long id;

    @ApiModelProperty(value = "操作模块")
    private String operModule;

    @ApiModelProperty(value = "操作类型 1增 2删 3改 4查")
    private Integer operType;

    @ApiModelProperty(value = "操作人员")
    private String operUser;

    @ApiModelProperty(value = "IP地址")
    private String ipAddress;

    @ApiModelProperty(value = "IP所属地")
    private String ipLocation;

    @ApiModelProperty(value = "操作结果 1成功 2失败")
    private Integer operResult;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @ApiModelProperty(value = "操作时间")
    private Date operTime;

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
    @ApiModelProperty(value = "逻辑删除 0正常 1删除")
    private Integer deleted;

    @TableField(exist = false)
    @ApiModelProperty(value = "操作时间起（查询）")
    private String operTimeStart;

    @TableField(exist = false)
    @ApiModelProperty(value = "操作时间止（查询）")
    private String operTimeEnd;
}
