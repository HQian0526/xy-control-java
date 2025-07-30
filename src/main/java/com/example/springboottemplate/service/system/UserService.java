package com.example.springboottemplate.service.system;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.system.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService {
    // 添加用户
    Response addUser(User user, HttpServletRequest request);

    // 查询用户列表
    Response findUser(User user, Integer pageNum, Integer pageSize);

    // 根据id查询用户信息
    Response getUserById(Long id);

    // 更新用户信息
    Response updateUser(User user);

    // 逻辑删除用户
    Response deleteUser(List<Integer> idList);

    // 查询当前用户信息
    Response getUserInfo(HttpServletRequest request);
}
