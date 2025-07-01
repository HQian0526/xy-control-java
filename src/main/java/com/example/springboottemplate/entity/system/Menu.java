package com.example.springboottemplate.entity.system;

import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor // 生成全参构造函数
@NoArgsConstructor // 生成无参构造函数
@ApiModel(description = "菜单管理")
public class Menu {
    @ApiModelProperty(value = "id", required = true)
    private Long id;

    @ApiModelProperty(value = "菜单名称", required = true)
    private String menuName = "";

    @ApiModelProperty(value = "菜单编码", required = true)
    private String menuCode = "";

    @ApiModelProperty(value = "父节点", required = false)
    private Long parentId;

    @ApiModelProperty(value = "节点类型：1文件夹 2页面 3按钮", required = true)
    private Integer menuType = 1;

    @ApiModelProperty(value = "图标地址", required = false)
    private String iconUrl = "";

    @ApiModelProperty(value = "排序", required = false)
    private Integer sort = 1;

    @ApiModelProperty(value = "层级", required = false)
    private Integer level = 0;

    @ApiModelProperty(value = "树id的路径", required = false)
    private String path = "";

    @ApiModelProperty(value = "前端组件路径", required = false)
    private String componentPath;

    @ApiModelProperty(value = "逻辑删除标识", required = false)
    @TableLogic
    private Integer deleted;
}
