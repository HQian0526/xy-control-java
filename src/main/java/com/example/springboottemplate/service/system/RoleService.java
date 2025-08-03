package com.example.springboottemplate.service.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboottemplate.dto.MenuTreeDto;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.RoleMenuDto;
import com.example.springboottemplate.dto.UserRoleDto;
import com.example.springboottemplate.entity.system.Role;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface RoleService extends IService<Role> {
    Response addRole(Role role, HttpServletRequest request);

    Response findRole(Role role, Integer pageNum, Integer pageSize);

    Response updateRole(Role role, HttpServletRequest request);

    Response deleteRole(List<Integer> idList);
    /**
     * 获取角色权限树
     */
    List<MenuTreeDto> getRoleMenuTree(Long roleId);

    /**
     * 分配权限
     */
    Response assignMenus(RoleMenuDto roleMenuDto);

    /**
     * 分配角色
     */
    Response assignRoles(UserRoleDto userRoleDto);

    /**
     * 根据用户id获取角色列表
     */
    Response getRoleList(Long userId);

    /**
     * 根据用户id获取角色列表
     */
    Response getUsersByRoleId(Long roleId);

}
