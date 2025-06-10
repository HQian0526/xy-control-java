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
@ApiModel(description = "商户信息")
public class Store {
    @ApiModelProperty(value = "id", required = true)
    private int id;

    @ApiModelProperty(value = "商户id", required = true)
    private int storeId;

    @ApiModelProperty(value = "商户名", required = true)
    private String name;

    @ApiModelProperty(value = "商户地址", required = true)
    private String address;

    @ApiModelProperty(value = "关联的userId", required = true)
    private int userId;

    @ApiModelProperty(value = "商户类型 1永久 2租用", required = true)
    private int storeType;

    @ApiModelProperty(value = "商户到期时间", required = false)
    private String storeTime;

    @ApiModelProperty(value = "商户状态 1正常 2异常 3冻结 4注销", required = false)
    private int stauts;

    @ApiModelProperty(value = "法人姓名", required = true)
    private String identityName;

    @ApiModelProperty(value = "工商主体名称", required = true)
    private String identityCompanyName;

    @ApiModelProperty(value = "法人身份证号", required = true)
    private String identityNo;

    @ApiModelProperty(value = "法人身份证正面base64", required = false)
    private String identityFontImg;

    @ApiModelProperty(value = "法人身份证反面base64", required = false)
    private String identityBackImg;

    @ApiModelProperty(value = "法人手机号", required = false)
    private String identityPhone;

    @ApiModelProperty(value = "法人邮箱", required = false)
    private String identityEmail;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

    @ApiModelProperty(value = "店铺创建时间", required = false)
    private String createdTime;

    @ApiModelProperty(value = "店铺信息创建人", required = false)
    private String createdBy;

    @ApiModelProperty(value = "信息更新时间", required = false)
    private String updateTime;

    @ApiModelProperty(value = "店铺信息更新人", required = false)
    private String updateBy;

    @ApiModelProperty(value = "逻辑删除标识", required = false)
    @TableLogic
    private String deleted;
}
