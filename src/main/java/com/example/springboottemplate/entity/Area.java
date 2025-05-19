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
@ApiModel(description = "桌台信息")
public class Area {
    @ApiModelProperty(value = "id", required = true)
    private int id;

    @ApiModelProperty(value = "区域id", required = true)
    private int areaId;

    @ApiModelProperty(value = "区域名", required = true)
    private String areaName;

    @ApiModelProperty(value = "所属店铺id", required = true)
    private int storeId;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

    @ApiModelProperty(value = "逻辑删除标识", required = false)
    @TableLogic
    private String deleted;
}
