package com.example.springboottemplate.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.system.SysRoleMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
    // 批量插入角色菜单关系
    void assignMenus(@Param("list") List<SysRoleMenu> list);

    // 根据角色ID查询菜单ID列表
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}
