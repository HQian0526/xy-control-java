package com.example.springboottemplate.controller;

import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.service.StoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/store")
@Api(tags = "商户管理", description = "商户相关接口")
public class StoreController {
    @Autowired
    private StoreService storeService;

    //添加商户
    @PostMapping("/addStore")
    @ResponseBody
    @ApiOperation(value = "添加商户", notes = "传入商户各项信息进行添加")
    public Response addStore(@RequestBody Store store, HttpServletRequest request){
        return storeService.addStore(store, request);
    }

    //查询所有商户
    @GetMapping("/findStore")
    @ResponseBody
    @ApiOperation(value = "查询所有商户", notes = "查询所有商户")
    public Response findStore(Store store, Integer pageNum, Integer pageSize){
        return storeService.findStore(store, pageNum, pageSize);
    }

    //修改商户信息
    @PutMapping("/updateStore")
    @ResponseBody
    @ApiOperation(value = "修改商户信息", notes = "根据id更新商户信息")
    public Response updateStore(@RequestBody Store store){
        return storeService.updateStore(store);
    }

    //删除商户信息（慎用）
    @DeleteMapping("/deleteStore")
    @ResponseBody
    @ApiOperation(value = "删除商户信息", notes = "根据id删除商户信息")
    public Response deleteStore(@RequestBody List<Integer> idList){
        return storeService.deleteStore(idList);
    }
}
