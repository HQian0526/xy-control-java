package com.example.springboottemplate.entity.contract;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor // 生成全参构造函数
@NoArgsConstructor // 生成无参构造函数
@ApiModel(description = "合同管理")
public class Contract {
    @ApiModelProperty(value = "id", required = true)
    private Long id;

    @ApiModelProperty(value = "合同编号", required = true)
    private String contractNo;

    @ApiModelProperty(value = "合同名称", required = true)
    private String contractName;

    @ApiModelProperty(value = "合同类型(1:租赁合同,2:销售合同...)", required = true)
    private int contractType;

    @ApiModelProperty(value = "出租方类型", required = true)
    private int lessorType;

    @ApiModelProperty(value = "出租方ID", required = true)
    private Long lessorId;

    @TableField(exist = false) // 表示该字段不在数据库中
    @ApiModelProperty(value = "出租方名称", required = false)
    private String lessorName;

    @ApiModelProperty(value = "承租方类型", required = true)
    private int lesseeType;

    @ApiModelProperty(value = "承租方ID", required = true)
    private Long lesseeId;

    @TableField(exist = false) // 表示该字段不在数据库中
    @ApiModelProperty(value = "承租方名称", required = false)
    private String lesseeName;

    @ApiModelProperty(value = "开始时间", required = false)
    private Date startDate;

    @ApiModelProperty(value = "结束时间", required = false)
    private Date endDate;

    @ApiModelProperty(value = "合同总金额", required = false)
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "付款周期(1一次性付清;2月付;3年付;4其他)", required = false)
    private int paymentCircle;

    @ApiModelProperty(value = "合同状态(1未签署;2已签署)", required = false)
    private int contractStatus;

    @ApiModelProperty(value = "签署日期", required = false)
    private Date signDate;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

    @ApiModelProperty(value = "创建时间", required = false)
    private Date createdTime;

    @ApiModelProperty(value = "合同创建人", required = false)
    private String createdBy;

    @ApiModelProperty(value = "合同更新时间", required = false)
    private Date updateTime;

    @ApiModelProperty(value = "合同更新人", required = false)
    private String updateBy;

    @ApiModelProperty(value = "逻辑删除标识", required = false)
    @TableLogic
    private Integer deleted;
}
