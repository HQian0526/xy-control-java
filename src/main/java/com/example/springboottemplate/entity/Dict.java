package com.example.springboottemplate.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonRawValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor // 生成全参构造函数
@NoArgsConstructor // 生成无参构造函数
@ApiModel(description = "字典管理")
@TableName(autoResultMap = true) // 启用自动结果映射
public class Dict {
    @ApiModelProperty(value = "id", required = true)
    private Integer id;

    @ApiModelProperty(value = "字典编码", required = true)
    private String code;

    @ApiModelProperty(value = "字典名称", required = true)
    private String dictName;

    @ApiModelProperty(value = "字典列表", required = true)
    private String dictJson;

    @ApiModelProperty(value = "字典备注", required = false)
    private String remark;

    @ApiModelProperty(value = "字典创建时间", required = false)
    private Date createdTime;

    @ApiModelProperty(value = "字典创建人", required = false)
    private String createdBy;

    @ApiModelProperty(value = "字典更新时间", required = false)
    private Date updateTime;

    @ApiModelProperty(value = "字典更新人", required = false)
    private String updateBy;

    @ApiModelProperty(value = "逻辑删除标识", required = false)
    @TableLogic
    private Integer deleted;
}
