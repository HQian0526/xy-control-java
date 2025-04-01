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
@ApiModel(description = "套餐信息")
public class Card {
    @ApiModelProperty(value = "id", required = true)
    private int id;

    @ApiModelProperty(value = "套餐id", required = true)
    private int cardId;

    @ApiModelProperty(value = "套餐名", required = true)
    private String name;

    @ApiModelProperty(value = "套餐价格", required = true)
    private String price;

    @ApiModelProperty(value = "套餐详情介绍", required = false)
    private String intro;

    @ApiModelProperty(value = "套餐类型 1小时卡 2次卡 3天卡 4周卡 5月卡 6季卡 7半年卡 8年卡 9其他", required = false)
    private int type;

    @ApiModelProperty(value = "套餐创建时间", required = false)
    private String createdTime;

    @ApiModelProperty(value = "套餐创建人", required = false)
    private int createdBy;

    @ApiModelProperty(value = "套餐状态 1启用 2停用 3删除", required = false)
    private int status;

    @ApiModelProperty(value = "套餐有效天数", required = false)
    private int effectDay;

    @ApiModelProperty(value = "套餐开始时间", required = false)
    private String startTime;

    @ApiModelProperty(value = "套餐结束时间", required = false)
    private String endTime;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

    @ApiModelProperty(value = "逻辑删除标识", required = false)
    @TableLogic
    private String deleted;
}
