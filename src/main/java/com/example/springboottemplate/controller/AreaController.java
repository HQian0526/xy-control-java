package com.example.springboottemplate.controller;

import com.example.springboottemplate.entity.Area;
import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.service.AreaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/area")
@Api(tags = "区域管理", description = "区域相关接口")
public class AreaController {
    @Autowired
    private AreaService areaService;

    // 新增用户
    @PostMapping("/addArea")
    @ResponseBody
    @ApiOperation(value = "添加区域", notes = "传入区域各项信息进行添加区域")
    public Response addArea(Area area){
        return areaService.addArea(area);
    }

    //查询所有区域
    @GetMapping("/findArea")
    @ResponseBody
    @ApiOperation(value = "查询所有区域", notes = "查询区域表中所有区域")
    public Response findArea(Area area){
        return areaService.findArea(area);
    }

    //修改区域信息
    @PutMapping("/updateArea")
    @ResponseBody
    @ApiOperation(value = "修改区域信息", notes = "根据id更新区域信息")
    public Response updateArea(Area area){
        return areaService.updateArea(area);
    }

    //删除区域信息
    @DeleteMapping("/deleteArea")
    @ResponseBody
    @ApiOperation(value = "删除区域", notes = "根据id删除区域")
    public Response deleteArea(List<Integer> idList){
        return areaService.deleteArea(idList);
    }
}
