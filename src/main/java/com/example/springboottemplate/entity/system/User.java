package com.example.springboottemplate.entity.system;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
@ApiModel(description = "用户信息")
public class User {
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "用户ID", required = true)
    private Integer id;

    @ApiModelProperty(value = "用户名", required = true)
    @com.fasterxml.jackson.annotation.JsonAlias({"username"})
    private String userName;

    @ApiModelProperty(value = "密码", required = false)
    private String password;

    @ApiModelProperty(value = "真实姓名", required = true)
    private String realName;

    @ApiModelProperty(value = "性别 0女 1男", required = true)
    private Integer sex;

    @ApiModelProperty(value = "手机号码", required = true)
    private String phone;

    @ApiModelProperty(value = "头像地址", required = false)
    private String avatar;

    @ApiModelProperty(value = "收货地址", required = false)
    private String address;

    @ApiModelProperty(value = "生日", required = false)
    private String birthday;

    @ApiModelProperty(value = "更新日期", required = false)
    private Date updateTime;

    @ApiModelProperty(value = "注册日期", required = false)
    private Date createdTime;

    @ApiModelProperty(value = "创建人", required = false)
    private String updateBy;

    @ApiModelProperty(value = "更新人", required = false)
    private String createdBy;

    @ApiModelProperty(value = "邮箱", required = false)
    private String email;

    @ApiModelProperty(value = "身份类型 1普通用户 2商户用户 3管理员", required = false)
    private Integer identityType;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "默认绑定的店铺id", required = false)
    private Long bindStoreId;

    @ApiModelProperty(value = "微信小程序 openid", required = false)
    private String openid;

    @ApiModelProperty(value = "微信 unionid（同一开放平台下打通多端）", required = false)
    private String unionid;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

    @ApiModelProperty(value = "逻辑删除标识", required = false)
    @TableLogic
    private Integer deleted;
}
