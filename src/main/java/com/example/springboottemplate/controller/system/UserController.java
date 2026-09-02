package com.example.springboottemplate.controller.system;

import com.example.springboottemplate.dto.ChangePasswordRequest;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.service.system.UserService;
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
    public Response addUser(@RequestBody User user, HttpServletRequest request){
        return userService.addUser(user, request);
    }

    //查询所有用户
    @GetMapping("/findUser")
    @ResponseBody
    @ApiOperation(value = "查询所有用户", notes = "查询用户表中所有用户")
    public Response findUser(User user, Integer pageNum, Integer pageSize){
        return userService.findUser(user, pageNum, pageSize);
    }

    //修改用户信息
    @PutMapping("/updateUser")
    @ResponseBody
    @ApiOperation(value = "修改用户信息", notes = "根据id更新用户信息")
    public Response updateUser(@RequestBody User user, HttpServletRequest request){
        return userService.updateUser(user, request);
    }

    //删除用户信息
    @DeleteMapping("/deleteUser")
    @ResponseBody
    @ApiOperation(value = "删除用户", notes = "根据id删除用户")
    public Response deleteUser(@RequestBody List<Long> idList){
        return userService.deleteUser(idList);
    }

    //获取当前登录用户信息
    @GetMapping("/getUserInfo")
    @ResponseBody
    @ApiOperation(value = "查询当前登录用户信息", notes = "查询当前登录用户信息")
    public Response getUserInfo(HttpServletRequest request) {
        return userService.getUserInfo(request);
    }

    // 重置密码（管理员）：按默认规则生成新密码，响应中返回一次明文
    @PostMapping("/resetPassword")
    @ResponseBody
    @ApiOperation(value = "重置用户密码", notes = "按配置前缀+MD5(当天yyyyMMdd)后6位生成新密码，data.newPassword 返回一次明文")
    public Response resetPassword(@RequestBody User user, HttpServletRequest request) {
        return userService.resetPassword(user != null ? user.getId() : null, request);
    }

    // 当前登录用户修改自己的密码
    @PostMapping("/changePassword")
    @ResponseBody
    @ApiOperation(value = "修改密码", notes = "已登录用户校验原密码后设置新密码")
    public Response changePassword(@RequestBody ChangePasswordRequest req,
                                   HttpServletRequest request) {
        return userService.changePassword(req, request);
    }
}
