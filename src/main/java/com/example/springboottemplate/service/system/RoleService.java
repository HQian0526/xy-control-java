package com.example.springboottemplate.service.system;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.system.Role;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface RoleService {
    Response addRole(Role role, HttpServletRequest request);

    Response findRole(Role role, Integer pageNum, Integer pageSize);

    Response updateRole(Role role);

    Response deleteRole(List<Integer> idList);
}
