package com.example.springboottemplate.serviceimpl.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboottemplate.dto.MenuTreeDto;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.RoleMenuDto;
import com.example.springboottemplate.dto.UserRoleDto;
import com.example.springboottemplate.entity.system.Menu;
import com.example.springboottemplate.entity.system.Role;
import com.example.springboottemplate.entity.system.SysRoleMenu;
import com.example.springboottemplate.entity.system.SysUserRole;
import com.example.springboottemplate.mapper.system.MenuMapper;
import com.example.springboottemplate.mapper.system.RoleMapper;
import com.example.springboottemplate.mapper.system.SysRoleMenuMapper;
import com.example.springboottemplate.mapper.system.SysUserRoleMapper;
import com.example.springboottemplate.service.system.MenuService;
import com.example.springboottemplate.service.system.RoleService;
import com.example.springboottemplate.utils.JwtUtil;
import com.example.springboottemplate.utils.ValidateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleServiceimpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private JwtUtil jwtUtil;  // 注入 JwtUtil
    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    @Autowired
    private MenuMapper menuMapper;
    @Autowired
    private MenuService menuService; // 注入MenuService

    @Override
    public Response addRole(Role role, HttpServletRequest request) {
        // 1. 从请求头中获取JWT令牌
        String token = request.getHeader("Authorization").substring(7);
        // 2. 解析令牌获取用户名
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        role.setCreatedTime(new Date());
        role.setCreatedBy(username);

        roleMapper.addRole(role);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findRole(Role role, Integer pageNum, Integer pageSize) {
        // 开启分页
        PageHelper.startPage(pageNum, pageSize);
        // 查询数据
        List<Role> list = roleMapper.findRole(role);
        // 封装分页结果
        PageInfo<Role> pageInfo = new PageInfo<>(list);
        // 构造返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getList());  // 当前页数据
        data.put("total", pageInfo.getTotal()); // 总记录数
        data.put("pages", pageInfo.getPages()); // 总页数
        data.put("pageNum", pageInfo.getPageNum()); // 当前页码
        data.put("pageSize", pageInfo.getPageSize()); // 每页数量
        return new Response(200, data, "操作成功");
    }

    @Override
    public Response updateRole(Role role) {
        roleMapper.updateRole(role);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteRole(List<Integer> idList) {
        if (ValidateUtil.isEmpty(idList)) {  // 使用工具类
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        int affectedRows = roleMapper.deleteBatchIds(idList); // 调用mybatis-plus的逻辑删除，返回受影响行数
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        } else {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
    }

    @Override
    public List<MenuTreeDto> getRoleMenuTree(Long roleId) {
        // 查询所有菜单
        List<Menu> menus = menuMapper.selectList(new QueryWrapper<Menu>()
                .orderByAsc("sort"));

        // 查询角色已有菜单ID
        List<Long> checkedKeys = Collections.emptyList();
        if (roleId != null) {
            checkedKeys = sysRoleMenuMapper.selectMenuIdsByRoleId(roleId);
        }

        // 构建菜单树
        return menuService.buildMenuTree(menus, checkedKeys);
    }

    @Override
    public Response assignMenus(RoleMenuDto roleMenuDTO) {
        Long roleId = roleMenuDTO.getRoleId();

        // 删除原有权限
        sysRoleMenuMapper.delete(new QueryWrapper<SysRoleMenu>()
                .eq("role_id", roleId));

        if (!CollectionUtils.isEmpty(roleMenuDTO.getMenuIds())) {
            // 构建关联关系
            List<SysRoleMenu> roleMenus = roleMenuDTO.getMenuIds().stream()
                    .map(menuId -> {
                        SysRoleMenu roleMenu = new SysRoleMenu();
                        roleMenu.setRoleId(roleId);
                        roleMenu.setMenuId(Long.valueOf(menuId));
                        return roleMenu;
                    })
                    .collect(Collectors.toList());

            // 批量插入
            sysRoleMenuMapper.assignMenus(roleMenus);
        }

        return Response.success();
    }

    @Override
    public Response assignRoles(UserRoleDto userRoleDTO) {
        Long userId = userRoleDTO.getUserId();

        // 删除原有权限
        sysUserRoleMapper.delete(new QueryWrapper<SysUserRole>()
                .eq("user_id", userId));

        if (!CollectionUtils.isEmpty(userRoleDTO.getRoleIds())) {
            // 构建关联关系
            List<SysUserRole> userRoles = userRoleDTO.getRoleIds().stream()
                    .map(roleId -> {
                        SysUserRole userRole = new SysUserRole();
                        userRole.setRoleId(userId);
                        userRole.setRoleId(Long.valueOf(roleId));
                        return userRole;
                    })
                    .collect(Collectors.toList());

            // 批量插入
            sysUserRoleMapper.assignRoles(userRoles);
        }

        return Response.success();
    }

    @Override
    public Response getRoleList(Long userId) {
        // 获取角色列表
        List<Long> list = sysUserRoleMapper.selectRoleIdsByUserId(userId);
        return Response.success(list);
    }
}
