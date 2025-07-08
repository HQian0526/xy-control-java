package com.example.springboottemplate.service.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboottemplate.dto.MenuTreeDto;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.RoleMenuDto;
import com.example.springboottemplate.entity.system.Role;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface RoleService extends IService<Role> {
    Response addRole(Role role, HttpServletRequest request);

    Response findRole(Role role, Integer pageNum, Integer pageSize);

    Response updateRole(Role role);

    Response deleteRole(List<Integer> idList);
    /**
     * 获取角色权限树
     */
    List<MenuTreeDto> getRoleMenuTree(Long roleId);

    /**
     * 分配权限
     */
    Response assignMenus(RoleMenuDto roleMenuDto);
}
