package com.example.springboottemplate.entity.system;

import com.baomidou.mybatisplus.annotation.TableLogic;
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
@ApiModel(description = "角色权限管理")
public class SysRoleMenu {
    @ApiModelProperty(value = "id", required = true)
    private Long id;

    @ApiModelProperty(value = "角色id", required = true)
    private Long roleId;

    @ApiModelProperty(value = "菜单id", required = true)
    private Long menuId;

    @ApiModelProperty(value = "授权时间", required = false)
    private Date createdTime;
}
