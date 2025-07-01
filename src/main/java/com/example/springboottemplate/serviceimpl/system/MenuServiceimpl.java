package com.example.springboottemplate.serviceimpl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboottemplate.dto.MenuRouteDto;
import com.example.springboottemplate.dto.MenuTreeDto;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.system.Menu;
import com.example.springboottemplate.mapper.system.MenuMapper;
import com.example.springboottemplate.service.system.MenuService;
import com.example.springboottemplate.utils.ValidateUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MenuServiceimpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {
    @Override
    public Response getMenuTree() {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getDeleted, 0)
                .orderByAsc(Menu::getSort);
        List<Menu> menus = baseMapper.selectList(wrapper);
        return Response.success(buildMenuTree(menus));
    }

    private List<MenuTreeDto> buildMenuTree(List<Menu> menus) {
        if (CollectionUtils.isEmpty(menus)) {
            return Collections.emptyList();
        }

        // 获取顶级菜单
        List<Menu> rootMenus = menus.stream()
                .filter(menu -> menu.getParentId() == null || menu.getParentId() == 0)
                .collect(Collectors.toList());

        List<MenuTreeDto> menuTree = rootMenus.stream()
                .map(menu -> convertToTreeDto(menu, menus))
                .sorted(Comparator.comparingInt(MenuTreeDto::getSort))
                .collect(Collectors.toList());

        return menuTree;
    }

    private MenuTreeDto convertToTreeDto(Menu menu, List<Menu> menus) {
        MenuTreeDto dto = new MenuTreeDto();
        dto.setId(String.valueOf(menu.getId()));
        dto.setMenuName(menu.getMenuName());
        dto.setMenuCode(menu.getMenuCode());
        dto.setParentId(String.valueOf(menu.getParentId()));
        dto.setMenuType(menu.getMenuType());
        dto.setIconUrl(menu.getIconUrl());
        dto.setSort(menu.getSort());
        dto.setComponentPath(menu.getComponentPath());
        dto.setLevel(menu.getLevel());
        dto.setPath(menu.getPath());

        // 查找子菜单
        List<Menu> children = menus.stream()
                .filter(m -> menu.getId().equals(m.getParentId()))
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(children)) {
            List<MenuTreeDto> childDtos = children.stream()
                    .map(m -> convertToTreeDto(m, menus))
                    .sorted(Comparator.comparingInt(MenuTreeDto::getSort))
                    .collect(Collectors.toList());
            dto.setChildren(childDtos);
        }

        return dto;
    }

    @Override
    public Response getMenuRoutes() {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getDeleted, 0)
                .orderByAsc(Menu::getSort);
        List<Menu> menus = baseMapper.selectList(wrapper);

        return Response.success(convertToRoutes(menus));
    }

    private List<MenuRouteDto> convertToRoutes(List<Menu> menus) {
        if (CollectionUtils.isEmpty(menus)) {
            return Collections.emptyList();
        }

        // 获取顶级菜单
        List<Menu> rootMenus = menus.stream()
                .filter(menu -> menu.getParentId() == null || menu.getParentId() == 0)
                .collect(Collectors.toList());

        List<MenuRouteDto> routes = rootMenus.stream()
                .map(menu -> convertToRouteDto(menu, menus))
                .sorted(Comparator.comparingInt(r -> {
                    Menu m = menus.stream().filter(menu -> menu.getMenuName().equals(r.getMeta().getTitle())).findFirst().orElse(null);
                    return m != null ? m.getSort() : 0;
                }))
                .collect(Collectors.toList());

        return routes;
    }

    private MenuRouteDto convertToRouteDto(Menu menu, List<Menu> menus) {
        MenuRouteDto dto = new MenuRouteDto();

        // 设置路由路径
        dto.setPath(menu.getPath() != null ? menu.getPath() : "/" + menu.getMenuCode());

        // 路由名称使用菜单编码
        dto.setName(menu.getMenuCode());

        // 设置组件
        if (menu.getMenuType() == 1) { // 文件夹
            dto.setComponent("");
        } else if (menu.getMenuType() == 2) { // 页面
            dto.setComponent(menu.getComponentPath().replace(":", "/"));
        }

        // 设置元数据
        MenuRouteDto.Meta meta = new MenuRouteDto.Meta();
        meta.setTitle(menu.getMenuName());
        meta.setIcon(menu.getIconUrl());
        meta.setHideMenu(false);
        meta.setHideBreadcrumb(false);
        meta.setHideTab(false);
        meta.setIgnoreKeepAlive(false);
        meta.setHideChildrenInMenu(menu.getMenuType() != 1);
        dto.setMeta(meta);

        // 查找子菜单
        List<Menu> children = menus.stream()
                .filter(m -> menu.getId().equals(m.getParentId()))
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(children)) {
            List<MenuRouteDto> childRoutes = children.stream()
                    .map(m -> convertToRouteDto(m, menus))
                    .sorted(Comparator.comparingInt(r -> {
                        Menu m = menus.stream().filter(child -> child.getMenuName().equals(r.getMeta().getTitle())).findFirst().orElse(null);
                        return m != null ? m.getSort() : 0;
                    }))
                    .collect(Collectors.toList());
            dto.setChildren(childRoutes);
        }

        return dto;
    }

    @Override
    public Response addMenu(Menu menu) {
        // 设置path和level
        if (menu.getParentId() == null || menu.getParentId() == 0) {
            menu.setLevel(1);
//            menu.setPath("0,");
        } else {
            Menu parent = baseMapper.selectById(menu.getParentId());
            if (parent != null) {
                menu.setLevel(parent.getLevel() + 1);
//                menu.setPath(parent.getPath() + parent.getId() + ",");
            }
        }
        if (baseMapper.insert(menu) > 0) {
            return Response.success();
        } else {
            return Response.fail("添加失败");
        }
    }

    @Override
    public Response updateMenu(Menu menu) {
        if (baseMapper.updateById(menu) > 0) {
            return Response.success();
        } else {
            return Response.fail("修改失败");
        }
    }

    @Override
    public Response deleteMenu(List<String> idList) {
        try {
            if (ValidateUtil.isEmpty(idList)) {  // 使用工具类
                return Response.fail("删除失败,id不能为空");
            }
            // 使用 Stream 转换
            List<Long> longList = idList.stream().map(s -> Long.parseLong(s)).collect(Collectors.toList());
            int affectedRows = baseMapper.deleteBatchIds(longList); // 调用mybatis-plus的逻辑删除，返回受影响行数
            if (affectedRows > 0) {
                return new Response(200, null, "操作成功");
            } else {
                return new Response(400, null, "操作失败，未找到需要删除的记录");
            }
//            Menu menu = new Menu();
//            menu.setId(Long.parseLong(id));
//            menu.setDeleted(1);
//            if (baseMapper.updateById(menu) > 0) {
//                return Response.success();
//            } else {
//                return Response.fail("删除失败");
//            }
        } catch(Exception e) {
            return Response.fail("删除失败");
        }

    }
}
