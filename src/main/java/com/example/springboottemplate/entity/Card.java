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
@ApiModel(description = "套餐信息")
public class Card {
    @ApiModelProperty(value = "id", required = true)
    private Integer id;

    @ApiModelProperty(value = "套餐id", required = true)
    private Integer cardId;

    @ApiModelProperty(value = "套餐名", required = true)
    private String cardName;

    @ApiModelProperty(value = "套餐价格", required = true)
    private String price;

    @ApiModelProperty(value = "套餐详情介绍", required = false)
    private String intro;

    @ApiModelProperty(value = "套餐类型 1小时卡 2次卡 3天卡 4周卡 5月卡 6季卡 7半年卡 8年卡 9其他", required = false)
    private Integer cardType;

    @ApiModelProperty(value = "套餐创建时间", required = false)
    private Date createdTime;

    @ApiModelProperty(value = "套餐创建人", required = false)
    private String createdBy;

    @ApiModelProperty(value = "套餐更新时间", required = false)
    private Date updateTime;

    @ApiModelProperty(value = "套餐更新人", required = false)
    private String updateBy;

    @ApiModelProperty(value = "套餐状态 1启用 2停用 3删除", required = false)
    private Integer status;

    @ApiModelProperty(value = "套餐有效天数", required = false)
    private Integer effectDay;

    @ApiModelProperty(value = "套餐开始时间", required = false)
    private String startTime;

    @ApiModelProperty(value = "套餐结束时间", required = false)
    private String endTime;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

    @ApiModelProperty(value = "逻辑删除标识", required = false)
    @TableLogic
    private Integer deleted;
}
