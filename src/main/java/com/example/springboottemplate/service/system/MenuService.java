package com.example.springboottemplate.service.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboottemplate.dto.MenuTreeDto;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.system.Menu;

import java.util.List;

public interface MenuService extends IService<Menu> {
    Response getMenuTree(Long roleId);

    Response getMenuRoutes();

    Response addMenu(Menu menu);

    Response updateMenu(Menu menu);

    Response deleteMenu(List<String> idList);

    // 暴露buildMenuTree方法
    List<MenuTreeDto> buildMenuTree(List<Menu> menus, List<Long> checkedMenuIds);
}
