package com.example.springboottemplate.entity;

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
@AllArgsConstructor // 生成全参构造函数
@NoArgsConstructor // 生成无参构造函数
@ApiModel(description = "设备操作记录")
public class EquUseLog {
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "id", required = true)
    private Integer id;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "操作用户的id", required = true)
    private Integer userId;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "操作的设备id", required = true)
    private Integer equId;

    @ApiModelProperty(value = "设备操作时间", required = true)
    private Date createdTime;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;
}
