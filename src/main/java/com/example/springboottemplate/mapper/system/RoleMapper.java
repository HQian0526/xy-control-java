package com.example.springboottemplate.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.system.Role;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    void addRole(Role role);  //新增角色

    List<Role> findRole(Role role); //查找所有角色

    void updateRole(Role role); //修改角色信息
}
