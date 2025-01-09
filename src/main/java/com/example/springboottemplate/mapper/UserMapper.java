package com.example.springboottemplate.mapper;

import com.example.springboottemplate.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    public void insert(User user);
}
