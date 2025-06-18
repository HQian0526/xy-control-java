package com.example.springboottemplate.service;

import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.entity.Role;

import java.util.List;

public interface RoleService {
    Response addRole(Role role);

    Response findRole(Role role, Integer pageNum, Integer pageSize);

    Response updateRole(Role role);

    Response deleteRole(List<Integer> idList);
}
