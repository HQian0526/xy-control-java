package com.example.springboottemplate.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.system.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    void addUser(User user);  //新增用户

    List<User> findUser(User user); //查找所有用户

    User selectByUserName(String userName); //根据用户名精确查询

    /** 按用户名查询（含逻辑删除），用于微信注册避免 user_name 唯一冲突 */
    User selectByUserNameAny(String userName);

    User selectByOpenid(String openid); //根据微信 openid 精确查询

    User selectByPhone(String phone); //根据手机号精确查询

    void updateUser(User user); //修改用户信息

    /** 解除微信身份并逻辑删除（合并账号时清理临时 wx 用户） */
    void detachWxAndSoftDelete(Integer id);
}
