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
@ApiModel(description = "设备信息")
public class Equ {
    @ApiModelProperty(value = "id", required = true)
    private Integer id; //自增id

    @ApiModelProperty(value = "设备id", required = true)
    private Integer equId;

    @ApiModelProperty(value = "设备名称", required = true)
    private String equName;

    @ApiModelProperty(value = "设备类型 1照明 2门禁 3其他", required = true)
    private Integer equType;

    @ApiModelProperty(value = "绑定的桌台", required = true)
    private Integer bindDeskId;

    @ApiModelProperty(value = "绑定的店铺", required = true)
    private Integer bindStoreId;

    @ApiModelProperty(value = "设备控制的三元码", required = false)
    private String equCode;

    @ApiModelProperty(value = "设备状态", required = false)
    private String equStatus;

    @ApiModelProperty(value = "设备创建时间", required = false)
    private Date createdTime;

    @ApiModelProperty(value = "创建人id", required = false)
    private String createdBy;

    @ApiModelProperty(value = "设备信息更新时间", required = false)
    private Date updateTime;

    @ApiModelProperty(value = "设备信息更新人", required = false)
    private String updateBy;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

    @ApiModelProperty(value = "逻辑删除标识", required = false)
    @TableLogic
    private Integer deleted;
}
