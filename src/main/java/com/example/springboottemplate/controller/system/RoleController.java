package com.example.springboottemplate.controller.system;

import com.example.springboottemplate.dto.MenuTreeDto;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.RoleMenuDto;
import com.example.springboottemplate.dto.UserRoleDto;
import com.example.springboottemplate.entity.system.Role;
import com.example.springboottemplate.service.system.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/role")
@Api(tags = "角色管理", description = "角色相关接口")
public class RoleController {
    @Autowired
    private RoleService roleService;

    // 新增角色
    @PostMapping("/addRole")
    @ResponseBody
    @ApiOperation(value = "添加角色", notes = "传入角色各项信息进行添加")
    public Response addRole(@RequestBody Role role, HttpServletRequest request){
        return roleService.addRole(role, request);
    }

    //查询所有角色
    @GetMapping("/findRole")
    @ResponseBody
    @ApiOperation(value = "查询所有角色", notes = "查询区域表中所有角色")
    public Response findRole(Role role, Integer pageNum, Integer pageSize){
        return roleService.findRole(role, pageNum, pageSize);
    }

    //修改角色信息
    @PutMapping("/updateRole")
    @ResponseBody
    @ApiOperation(value = "修改角色信息", notes = "根据id更新角色信息")
    public Response updateRole(@RequestBody Role role){
        return roleService.updateRole(role);
    }

    //删除角色信息
    @DeleteMapping("/deleteRole")
    @ResponseBody
    @ApiOperation(value = "删除角色", notes = "根据id删除角色")
    public Response deleteRole(@RequestBody List<Integer> idList){
        return roleService.deleteRole(idList);
    }

    //获取角色权限树
    @GetMapping("/menu-tree/{roleId}")
    @ResponseBody
    @ApiOperation(value = "获取角色权限树", notes = "获取某个角色的权限菜单树")
    public Response getRoleMenuTree(@PathVariable Long roleId) {
        List<MenuTreeDto> menuTree = roleService.getRoleMenuTree(roleId);
        return Response.success(menuTree);
    }

    //角色授权菜单
    @PostMapping("/assignPerms")
    @ResponseBody
    @ApiOperation(value = "角色授权", notes = "给某个角色授予特定菜单权限")
    public Response assignMenus(@RequestBody RoleMenuDto roleMenuDto) {
        return roleService.assignMenus(roleMenuDto);
    }

    //用户授予角色
    @PostMapping("/assignRoles")
    @ResponseBody
    @ApiOperation(value = "用户授予角色", notes = "给某个用户授予特定角色")
    public Response assignRoles(@RequestBody UserRoleDto userRoleDto) {
        return roleService.assignRoles(userRoleDto);
    }

    //获取用户角色列表
    @GetMapping("/getRoleList/{userId}")
    @ResponseBody
    @ApiOperation(value = "获取用户角色列表", notes = "获取某个用户的角色列表")
    public Response getRoleList(@PathVariable Long userId) {
        return roleService.getRoleList(userId);
    }
}
