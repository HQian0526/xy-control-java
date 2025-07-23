package com.example.springboottemplate.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserRoleDto {
    private Long userId;

    private Long roleId;

    private List<Long> roleIds;

    private List<Long> userIds;
}
