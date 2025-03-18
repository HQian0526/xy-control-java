package com.example.springboottemplate.mapper;

import com.example.springboottemplate.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    public void addUser(User user);  //新增用户

    public List<User> findUser(); //查找所有用户

    public List<User> selectUserById(int id); //根据id查找用户

    public void updateUser(User user); //修改用户信息

    public void deleteUser(int id); //删除用户信息
}
