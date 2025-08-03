package com.example.springboottemplate.entity;

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
@ApiModel(description = "区域信息")
public class Area {
    @ApiModelProperty(value = "id", required = true)
    private Long id;

    @ApiModelProperty(value = "区域id", required = true)
    private Long areaId;

    @ApiModelProperty(value = "区域名", required = true)
    private String areaName;

    @ApiModelProperty(value = "所属店铺id", required = true)
    private Long storeId;

    @ApiModelProperty(value = "所属店铺名", required = false)
    private String storeName;

    @ApiModelProperty(value = "区域创建时间", required = false)
    private Date createdTime;

    @ApiModelProperty(value = "区域创建人", required = false)
    private String createdBy;

    @ApiModelProperty(value = "区域更新时间", required = false)
    private Date updateTime;

    @ApiModelProperty(value = "区域更新人", required = false)
    private String updateBy;

    @ApiModelProperty(value = "区域照片", required = false)
    private String areaPhoto;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

    @ApiModelProperty(value = "逻辑删除标识", required = false)
    @TableLogic
    private Integer deleted;
}
