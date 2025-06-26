package com.example.springboottemplate.controller;

import com.example.springboottemplate.entity.Equ;
import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.service.EquService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/equ")
@Api(tags = "设备管理", description = "设备相关接口")
public class EquController {
    @Autowired
    private EquService equService;

    // 添加设备
    @PostMapping("/addEqu")
    @ResponseBody
    @ApiOperation(value = "添加设备", notes = "传入用户各项信息进行添加设备")
    public Response addEqu(@RequestBody Equ equ, HttpServletRequest request){
        return equService.addEqu(equ, request);
    }

    //模糊查询所有设备
    @GetMapping("/findEqu")
    @ResponseBody
    @ApiOperation(value = "查询所有设备", notes = "查询设备表中所有设备")
    public Response findEqu(Equ equ, Integer pageNum, Integer pageSize){
        return equService.findEqu(equ, pageNum, pageSize);
    }

    //修改设备信息
    @PutMapping("/updateEqu")
    @ResponseBody
    @ApiOperation(value = "修改设备信息", notes = "根据id更新设备信息")
    public Response updateEqu(@RequestBody Equ equ){
        return equService.updateEqu(equ);
    }

    //删除设备信息（慎用）
    @DeleteMapping("/deleteEqu")
    @ResponseBody
    @ApiOperation(value = "删除设备", notes = "根据id删除设备")
    public Response deleteEqu(@RequestBody List<Integer> idList){
        return equService.deleteEqu(idList);
    }
}
