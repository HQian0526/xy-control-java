package com.example.springboottemplate.controller;

import com.example.springboottemplate.entity.Catagory;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.service.CatagoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/catagory")
@Api(tags = "商品分类管理", description = "商品分类相关接口")
public class CatagoryController {
    @Autowired
    private CatagoryService catagoryService;

    // 新增商品分类
    @PostMapping("/addCatagory")
    @ResponseBody
    @ApiOperation(value = "添加商品分类", notes = "传入分类各项信息进行添加")
    public Response addCatagory(@RequestBody Catagory catagory, HttpServletRequest request){
        return catagoryService.addCatagory(catagory, request);
    }

    //查询所有商品分类
    @GetMapping("/findCatagory")
    @ResponseBody
    @ApiOperation(value = "查询所有商品分类", notes = "按当前用户身份返回分类：普通用户空、商户仅本店、管理员全部")
    public Response findCatagory(Catagory catagory, Integer pageNum, Integer pageSize, HttpServletRequest request){
        return catagoryService.findCatagory(catagory, pageNum, pageSize, request);
    }

    //修改商品分类信息
    @PutMapping("/updateCatagory")
    @ResponseBody
    @ApiOperation(value = "修改商品分类信息", notes = "根据id更新分类信息")
    public Response updateCatagory(@RequestBody Catagory catagory, HttpServletRequest request){
        return catagoryService.updateCatagory(catagory, request);
    }

    //删除商品分类信息
    @DeleteMapping("/deleteCatagory")
    @ResponseBody
    @ApiOperation(value = "删除商品分类", notes = "根据id删除分类")
    public Response deleteCatagory(@RequestBody List<Long> idList){
        return catagoryService.deleteCatagory(idList);
    }
}
