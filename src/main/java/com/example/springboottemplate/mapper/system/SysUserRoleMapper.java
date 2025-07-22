package com.example.springboottemplate.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.system.SysUserRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    // 批量插入用户角色关系
    void assignRoles(@Param("list") List<SysUserRole> list);

    // 根据用户ID查询角色ID列表
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}
