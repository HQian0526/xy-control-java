package com.example.springboottemplate.controller;

import com.example.springboottemplate.annotation.OperLog;
import com.example.springboottemplate.annotation.OperTypes;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.StorePrinterSaveRequest;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.service.StorePrinterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/storePrinter")
@Api(tags = "店铺云打印机", description = "管理员代配各店飞鹅打印机")
public class StorePrinterController {

    @Autowired
    private StorePrinterService storePrinterService;

    @GetMapping
    @ResponseBody
    @ApiOperation("查询店铺打印机")
    public Response get(@RequestParam Long storeId, HttpServletRequest request) {
        try {
            return storePrinterService.getByStoreId(storeId, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation("店铺打印机列表")
    public Response list(HttpServletRequest request) {
        try {
            return storePrinterService.list(request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @PutMapping
    @ResponseBody
    @ApiOperation("保存并绑定飞鹅打印机")
    @OperLog(module = "云打印机", type = OperTypes.UPDATE)
    public Response save(@RequestBody StorePrinterSaveRequest body, HttpServletRequest request) {
        try {
            return storePrinterService.save(body, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("保存失败: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    @ResponseBody
    @ApiOperation("查询打印机在线状态")
    public Response status(@RequestParam Long storeId, HttpServletRequest request) {
        try {
            return storePrinterService.queryStatus(storeId, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/testPrint")
    @ResponseBody
    @ApiOperation("打一张测试单")
    @OperLog(module = "云打印机", type = OperTypes.UPDATE, remark = "测试打印")
    public Response testPrint(@RequestParam Long storeId, HttpServletRequest request) {
        try {
            return storePrinterService.testPrint(storeId, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("测试打印失败: " + e.getMessage());
        }
    }

    @PostMapping("/reprint")
    @ResponseBody
    @ApiOperation("补打已支付订单小票，本店商家或管理员")
    @OperLog(module = "云打印机", type = OperTypes.UPDATE, remark = "补打小票")
    public Response reprint(@RequestParam String orderNo, HttpServletRequest request) {
        try {
            return storePrinterService.reprint(orderNo, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("补打失败: " + e.getMessage());
        }
    }
}
