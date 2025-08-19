package com.example.springboottemplate.entity.contract;

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
@ApiModel(description = "合同模板管理")
public class ContractTemplate {
    @ApiModelProperty(value = "模板ID", required = true)
    private Long id;

    @ApiModelProperty(value = "模板名称", required = true)
    private String templateName;

    @ApiModelProperty(value = "模板类型(1:租赁合同,2:销售合同...)", required = true)
    private Integer templateType;

    @ApiModelProperty(value = "模板文件路径", required = true)
    private String filePath;

    @ApiModelProperty(value = "模板变量配置(JSON格式)")
    private String variablesConfig;

    @ApiModelProperty(value = "创建时间")
    private Date createdTime;

    @ApiModelProperty(value = "更新时间")
    private Date updatedTime;

    @ApiModelProperty(value = "逻辑删除标识", required = false)
    @TableLogic
    private Integer deleted;
}
