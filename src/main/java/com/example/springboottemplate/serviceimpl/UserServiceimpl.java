package com.example.springboottemplate.serviceimpl;
import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.entity.User;
import com.example.springboottemplate.mapper.UserMapper;
import com.example.springboottemplate.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceimpl implements UserService {

    @Autowired
    private UserMapper Usermapper;
    @Override
    public Response addUser(User user) {
        this.Usermapper.addUser(user);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findUser(User user) {
        List<User> list = (List<User>) this.Usermapper.findUser(user);
        return new Response(200, list, "操作成功");
    }

    @Override
    public Response updateUser(User user) {
        this.Usermapper.updateUser(user);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteUser(int id) {
        this.Usermapper.deleteUser(id);
        return new Response(200, null, "操作成功");
    }
}
