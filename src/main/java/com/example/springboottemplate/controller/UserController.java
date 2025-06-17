package com.example.springboottemplate.controller;

import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.entity.User;
import com.example.springboottemplate.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;

@Controller
@RequestMapping("/user")
@Api(tags = "用户管理", description = "用户相关接口")
public class UserController {
    @Autowired
    private UserService userService;

    // 新增用户
    @PostMapping("/addUser")
    @ResponseBody
    @ApiOperation(value = "添加用户", notes = "传入用户各项信息进行添加用户")
    public Response addUser(User user){
        return userService.addUser(user);
    }

    //查询所有用户
    @GetMapping("/findUser")
    @ResponseBody
    @ApiOperation(value = "查询所有用户", notes = "查询用户表中所有用户")
    public Response findUser(User user){
        return userService.findUser(user);
    }

    //修改用户信息
    @PutMapping("/updateUser")
    @ResponseBody
    @ApiOperation(value = "修改用户信息", notes = "根据id更新用户信息")
    public Response updateUser(User user){
        return userService.updateUser(user);
    }

    //删除用户信息
    @DeleteMapping("/deleteUser")
    @ResponseBody
    @ApiOperation(value = "删除用户", notes = "根据id删除用户")
    public Response deleteUser(List<Integer> idList){
        return userService.deleteUser(idList);
    }

    //获取当前登录用户信息
    @GetMapping("/getUserInfo")
    @ResponseBody
    @ApiOperation(value = "查询当前登录用户信息", notes = "查询当前登录用户信息")
    public Response getUserInfo(HttpServletRequest request) {
        return userService.getUserInfo(request);
    }
}
