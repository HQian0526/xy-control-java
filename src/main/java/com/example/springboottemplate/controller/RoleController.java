package com.example.springboottemplate.controller;

import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.entity.Role;
import com.example.springboottemplate.service.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
    public Response addRole(Role role){
        return roleService.addRole(role);
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
    public Response updateRole(Role role){
        return roleService.updateRole(role);
    }

    //删除角色信息
    @DeleteMapping("/deleteRole")
    @ResponseBody
    @ApiOperation(value = "删除角色", notes = "根据id删除角色")
    public Response deleteRole(List<Integer> idList){
        return roleService.deleteRole(idList);
    }
}
