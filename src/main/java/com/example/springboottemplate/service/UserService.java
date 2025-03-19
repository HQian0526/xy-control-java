package com.example.springboottemplate.service;

import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.entity.User;

public interface UserService {
    public Response addUser(User user);

    public Response findUser(User user);

    public Response updateUser(User user);

    public Response deleteUser(int id);
}
