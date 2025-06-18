package com.example.springboottemplate.service;

import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService {
    Response addUser(User user);

    Response findUser(User user, Integer pageNum, Integer pageSize);

    Response updateUser(User user);

    Response deleteUser(List<Integer> idList);

    Response getUserInfo(HttpServletRequest request);
}
