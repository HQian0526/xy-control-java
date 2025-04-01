package com.example.springboottemplate.service;

import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.entity.User;

import java.util.List;

public interface UserService {
    Response addUser(User user);

    Response findUser(User user);

    Response updateUser(User user);

    Response deleteUser(List<Integer> idList);
}
