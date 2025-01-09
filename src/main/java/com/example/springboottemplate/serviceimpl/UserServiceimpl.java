package com.example.springboottemplate.serviceimpl;
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
    public void adduser(User user) {
        this.Usermapper.insert(user);
    }
}
