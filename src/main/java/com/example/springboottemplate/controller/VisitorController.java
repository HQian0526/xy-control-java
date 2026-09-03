package com.example.springboottemplate.controller;

import com.example.springboottemplate.annotation.OperLog;
import com.example.springboottemplate.annotation.OperTypes;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.Visitor;
import com.example.springboottemplate.service.VisitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/visitor")
@Api(tags = "来客管理", description = "来客信息相关接口")
public class VisitorController {
    @Autowired
    private VisitorService visitorService;

    @PostMapping("/addVisitor")
    @ResponseBody
    @ApiOperation(value = "添加来客信息", notes = "传入姓名、联系方式等进行添加")
    @OperLog(module = "来客管理", type = OperTypes.ADD)
    public Response addVisitor(@RequestBody Visitor visitor, HttpServletRequest request) {
        return visitorService.addVisitor(visitor, request);
    }

    @GetMapping("/findVisitor")
    @ResponseBody
    @ApiOperation(value = "查询来客信息", notes = "按当前用户身份返回：普通用户空、商户仅本店、管理员全部")
    public Response findVisitor(Visitor visitor, Integer pageNum, Integer pageSize, HttpServletRequest request) {
        return visitorService.findVisitor(visitor, pageNum, pageSize, request);
    }

    @PutMapping("/updateVisitor")
    @ResponseBody
    @ApiOperation(value = "修改来客信息", notes = "根据id更新来客信息")
    @OperLog(module = "来客管理", type = OperTypes.UPDATE)
    public Response updateVisitor(@RequestBody Visitor visitor, HttpServletRequest request) {
        return visitorService.updateVisitor(visitor, request);
    }

    @DeleteMapping("/deleteVisitor")
    @ResponseBody
    @ApiOperation(value = "删除来客信息", notes = "根据id删除来客（逻辑删除）")
    @OperLog(module = "来客管理", type = OperTypes.DELETE)
    public Response deleteVisitor(@RequestBody List<Long> idList) {
        return visitorService.deleteVisitor(idList);
    }
}
