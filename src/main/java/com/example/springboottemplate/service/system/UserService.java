package com.example.springboottemplate.service.system;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.system.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService {
    Response addUser(User user, HttpServletRequest request);

    Response findUser(User user, Integer pageNum, Integer pageSize);

    Response updateUser(User user);

    Response deleteUser(List<Integer> idList);

    Response getUserInfo(HttpServletRequest request);
}
