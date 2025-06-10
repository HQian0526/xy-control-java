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
@ApiModel(description = "用户信息")
public class User {
    @ApiModelProperty(value = "用户ID", required = true)
    private int id;

    @ApiModelProperty(value = "用户名", required = true)
    private String userName;

    @ApiModelProperty(value = "真实姓名", required = true)
    private String realName;

    @ApiModelProperty(value = "性别 0女 1男", required = true)
    private int sex;

    @ApiModelProperty(value = "手机号码", required = true)
    private String phone;

    @ApiModelProperty(value = "收货地址", required = false)
    private String address;

    @ApiModelProperty(value = "生日", required = false)
    private String birthday;

    @ApiModelProperty(value = "更新日期", required = false)
    private String updateTime;

    @ApiModelProperty(value = "注册日期", required = false)
    private String createdTime;

    @ApiModelProperty(value = "邮箱", required = false)
    private String email;

    @ApiModelProperty(value = "会员等级 1基础用户 2vip会员 3管理员", required = false)
    private String identity;

    @ApiModelProperty(value = "逻辑删除标识", required = false)
    @TableLogic
    private String deleted;
}
