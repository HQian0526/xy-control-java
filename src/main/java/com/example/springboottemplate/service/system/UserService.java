package com.example.springboottemplate.service.system;

import com.example.springboottemplate.dto.ChangePasswordRequest;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.system.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService {
    // 添加用户
    Response addUser(User user, HttpServletRequest request);

    // 查询用户列表
    Response findUser(User user, Integer pageNum, Integer pageSize);

    // 查询店铺顾客（已进店且未拉黑）
    Response findCustomer(User user, Integer pageNum, Integer pageSize, HttpServletRequest request);

    // 根据id查询用户信息
    Response getUserById(Long id);

    // 更新用户信息
    Response updateUser(User user, HttpServletRequest request);

    // 当前登录用户修改自己的资料（昵称/性别/默认收货地址）
    Response updateProfile(User user, HttpServletRequest request);

    // 逻辑删除用户
    Response deleteUser(List<Long> idList);

    // 查询当前用户信息
    Response getUserInfo(HttpServletRequest request);

    // 重置用户密码（按默认规则生成，返回一次明文）
    Response resetPassword(Long id, HttpServletRequest request);

    // 当前登录用户修改密码
    Response changePassword(ChangePasswordRequest req, HttpServletRequest request);
}
