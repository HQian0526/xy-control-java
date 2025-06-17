package com.example.springboottemplate.entity;

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
@ApiModel(description = "字典管理")
public class Dict {
    @ApiModelProperty(value = "id", required = true)
    private int id;

    @ApiModelProperty(value = "字典编码", required = true)
    private String code;

    @ApiModelProperty(value = "字典名称", required = true)
    private String dictName;

    @ApiModelProperty(value = "字典列表", required = true)
    private String dictJson;

    @ApiModelProperty(value = "字典备注", required = false)
    private String remark;

    @ApiModelProperty(value = "逻辑删除标识", required = false)
    @TableLogic
    private int deleted;
}
