package com.example.springboottemplate.controller;

import com.example.springboottemplate.entity.User;
import com.example.springboottemplate.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping("/adduser")
    public String addUser(User user){
        this.userService.adduser(user);
        return "success";
    }
}
