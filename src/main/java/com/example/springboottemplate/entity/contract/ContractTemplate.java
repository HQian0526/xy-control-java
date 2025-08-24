package com.example.springboottemplate.entity.contract;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "模板ID", required = true)
    private Integer id;

    @ApiModelProperty(value = "模板编码", required = true)
    private String templateNo;

    @ApiModelProperty(value = "模板名称", required = true)
    private String templateName;

    @ApiModelProperty(value = "模板类型(1:租赁合同,2:销售合同...)", required = true)
    private Integer templateType;

    @ApiModelProperty(value = "模板文件路径", required = false)
    private String fileId;

    @ApiModelProperty(value = "模板文件路径", required = false)
    private String fileName;

    @ApiModelProperty(value = "模板文件路径", required = false)
    private String filePath;

    @ApiModelProperty(value = "模板文件路径", required = false)
    private String fileSize;

    @ApiModelProperty(value = "模板变量配置(JSON格式)")
    private String variablesConfig;

    @ApiModelProperty(value = "创建时间")
    private Date createdTime;

    @ApiModelProperty(value = "合同创建人", required = false)
    private String createdBy;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "合同更新人", required = false)
    private String updateBy;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

    @ApiModelProperty(value = "逻辑删除标识", required = false)
    @TableLogic
    private Integer deleted;
}
