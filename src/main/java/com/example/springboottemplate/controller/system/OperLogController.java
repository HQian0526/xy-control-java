package com.example.springboottemplate.controller.system;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.system.OperLog;
import com.example.springboottemplate.service.system.OperLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/operLog")
@Api(tags = "操作日志", description = "操作日志相关接口")
public class OperLogController {

    @Autowired
    private OperLogService operLogService;

    @PostMapping("/addOperLog")
    @ResponseBody
    @ApiOperation(value = "新增操作日志", notes = "新增一条操作日志；id/operTime 可不传")
    public Response addOperLog(@RequestBody OperLog operLog, HttpServletRequest request) {
        return operLogService.addOperLog(operLog, request);
    }

    @GetMapping("/findOperLog")
    @ResponseBody
    @ApiOperation(value = "查询操作日志", notes = "支持模块/类型/人员/结果/时间范围筛选与分页")
    public Response findOperLog(OperLog operLog, Integer pageNum, Integer pageSize) {
        return operLogService.findOperLog(operLog, pageNum, pageSize);
    }

    @PutMapping("/updateOperLog")
    @ResponseBody
    @ApiOperation(value = "修改操作日志", notes = "根据id更新操作日志")
    public Response updateOperLog(@RequestBody OperLog operLog, HttpServletRequest request) {
        return operLogService.updateOperLog(operLog, request);
    }

    @DeleteMapping("/deleteOperLog")
    @ResponseBody
    @ApiOperation(value = "删除操作日志", notes = "根据id列表逻辑删除")
    public Response deleteOperLog(@RequestBody List<Long> idList) {
        return operLogService.deleteOperLog(idList);
    }
}
