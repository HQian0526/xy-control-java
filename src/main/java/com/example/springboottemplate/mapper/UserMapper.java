package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    void addUser(User user);  //新增用户

    List<User> findUser(User user); //查找所有用户

    List<User> selectUserById(int id); //根据id查找用户

    void updateUser(User user); //修改用户信息

//    public void deleteUser(int id); //删除用户信息
}
