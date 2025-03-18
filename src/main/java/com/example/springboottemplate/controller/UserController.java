package com.example.springboottemplate.controller;

import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.entity.User;
import com.example.springboottemplate.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    //添加用户
    @PostMapping("/addUser")
    @ResponseBody
    public Response addUser(User user){
        return this.userService.addUser(user);
    }

    //查询所有用户
    @GetMapping("/findUser")
    @ResponseBody
    public Response findUser(){
        return this.userService.findUser();
    }

    //根据id查询用户信息
    @GetMapping("/selectUserById")
    @ResponseBody
    public Response selectUserById(int id){
        return this.userService.selectUserById(id);
    }

    //修改用户信息
    @PutMapping("/updateUser")
    @ResponseBody
    public Response updateUser(User user){
        return this.userService.updateUser(user);
    }

    //删除用户信息（慎用）
    @DeleteMapping("/deleteUser")
    @ResponseBody
    public Response deleteUser(int id){
        return this.userService.deleteUser(id);
    }
}
