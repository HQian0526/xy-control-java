package com.example.springboottemplate.controller;

import com.example.springboottemplate.entity.EquUseLog;
import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.service.EquUseLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/equUseLog")
@Api(tags = "设备使用记录管理", description = "设备使用记录相关接口")
public class EquUseLogController {
    @Autowired
    private EquUseLogService equUseLogService;

    // 添加设备使用记录
    @PostMapping("/addEquUseLog")
    @ResponseBody
    @ApiOperation(value = "添加设备使用记录", notes = "添加设备使用记录")
    public Response addEquUseLog(EquUseLog equUseLog){
        return equUseLogService.addEquUseLog(equUseLog);
    }

    //模糊查询所有设备使用记录
    @GetMapping("/findEquUseLog")
    @ResponseBody
    @ApiOperation(value = "查询所有设备使用记录", notes = "查询设备表中所有设备使用记录")
    public Response findEquUseLog(EquUseLog equUseLog){
        return equUseLogService.findEquUseLog(equUseLog);
    }

    //修改设备使用记录信息
    @PutMapping("/updateEquUseLog")
    @ResponseBody
    @ApiOperation(value = "修改设备使用记录信息", notes = "根据id更新设备使用记录信息")
    public Response updateEquUseLog(EquUseLog equUseLog){
        return equUseLogService.updateEquUseLog(equUseLog);
    }

    //删除设备使用记录信息
    @DeleteMapping("/deleteEquUseLog")
    @ResponseBody
    @ApiOperation(value = "删除设备使用记录", notes = "根据id删除设备使用记录")
    public Response deleteEquUseLog(List<Integer> idList){
        return equUseLogService.deleteEquUseLog(idList);
    }
}
