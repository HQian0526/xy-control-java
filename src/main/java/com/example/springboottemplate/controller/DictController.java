package com.example.springboottemplate.controller;

import com.example.springboottemplate.entity.Dict;
import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/dict")
@Api(tags = "字典管理", description = "字典管理相关接口")
public class DictController {
    @Autowired
    private DictService dictService;

    // 新增字典
    @PostMapping("/addDict")
    @ResponseBody
    @ApiOperation(value = "添加字典", notes = "传入字典信息进行添加字典")
    public Response addDict(Dict dict){
        return dictService.addDict(dict);
    }

    //查询所有字典
    @GetMapping("/findDict")
    @ResponseBody
    @ApiOperation(value = "查询所有字典", notes = "查询字典表中所有字典")
    public Response findDict(Dict dict, Integer pageNum, Integer pageSize){
        return dictService.findDict(dict, pageNum, pageSize);
    }

    //修改字典信息
    @PutMapping("/updateDict")
    @ResponseBody
    @ApiOperation(value = "修改字典信息", notes = "根据id更新字典信息")
    public Response updateDict(Dict dict){
        return dictService.updateDict(dict);
    }

    //删除字典信息
    @DeleteMapping("/deleteDict")
    @ResponseBody
    @ApiOperation(value = "删除字典", notes = "根据id删除字典")
    public Response deleteDict(List<Integer> idList){
        return dictService.deleteDict(idList);
    }
}
