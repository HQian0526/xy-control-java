package com.example.springboottemplate.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.system.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    void addUser(User user);  //新增用户

    List<User> findUser(User user); //查找所有用户

    void updateUser(User user); //修改用户信息
}
