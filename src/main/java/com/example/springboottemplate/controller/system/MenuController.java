package com.example.springboottemplate.controller.system;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.system.Menu;
import com.example.springboottemplate.service.system.MenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/menu")
@Api(tags = "菜单管理", description = "菜单管理相关接口")
public class MenuController {
    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/findMenu")
    @ResponseBody
    @ApiOperation(value = "获取菜单树", notes = "获取菜单树")
    public Response getMenuTree() {
        return menuService.getMenuTree();
    }

    @GetMapping("/getMenuRoute")
    @ResponseBody
    @ApiOperation(value = "获取菜单路由", notes = "获取菜单路由")
    public Response getMenuRoutes() {
        return menuService.getMenuRoutes();
    }

    @PostMapping("/addMenu")
    @ResponseBody
    @ApiOperation(value = "添加菜单", notes = "添加菜单")
    public Response addMenu(@RequestBody Menu menu) {
        return menuService.addMenu(menu);
    }

    @PutMapping("/updateMenu")
    @ResponseBody
    @ApiOperation(value = "更新菜单", notes = "更新菜单")
    public Response updateMenu(@RequestBody Menu menu) {
        return menuService.updateMenu(menu);
    }

    @DeleteMapping("/deleteMenu")
    @ResponseBody
    @ApiOperation(value = "删除菜单", notes = "删除菜单")
    public Response deleteMenu(@RequestBody List<String> idList) {
        return menuService.deleteMenu(idList);
    }
}
