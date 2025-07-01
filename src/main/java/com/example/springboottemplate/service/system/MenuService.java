package com.example.springboottemplate.service.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.system.Menu;

public interface MenuService extends IService<Menu> {
    Response getMenuTree();

    Response getMenuRoutes();

    boolean addMenu(Menu menu);

    boolean updateMenu(Menu menu);

    boolean deleteMenu(Long id);
}
