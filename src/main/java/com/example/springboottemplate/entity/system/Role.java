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
@ApiModel(description = "角色管理")
public class Role {
    @ApiModelProperty(value = "id", required = true)
    private Integer id;

    @ApiModelProperty(value = "角色id", required = true)
    private Integer roleId;

    @ApiModelProperty(value = "角色名称", required = true)
    private String roleName;

    @ApiModelProperty(value = "角色备注", required = false)
    private String remark;

    @ApiModelProperty(value = "角色对应的用户组", required = false)
    private String userList;

    @ApiModelProperty(value = "角色对应的权限菜单", required = false)
    private String authList;

    @ApiModelProperty(value = "角色创建时间", required = false)
    private Date createdTime;

    @ApiModelProperty(value = "角色创建人", required = false)
    private String createdBy;

    @ApiModelProperty(value = "角色更新时间", required = false)
    private Date updateTime;

    @ApiModelProperty(value = "角色更新人", required = false)
    private String updateBy;

    @ApiModelProperty(value = "逻辑删除标识", required = false)
    @TableLogic
    private Integer deleted;
}
