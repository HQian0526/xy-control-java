package com.example.springboottemplate.entity.system;

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
@ApiModel(description = "用户-角色关联表")
public class SysUserRole {
    @ApiModelProperty(value = "id", required = true)
    private Long id;

    @ApiModelProperty(value = "用户id", required = true)
    private Long userId;

    @ApiModelProperty(value = "角色id", required = true)
    private Long roleId;

    @ApiModelProperty(value = "授权时间", required = false)
    private Date createdTime;
}
