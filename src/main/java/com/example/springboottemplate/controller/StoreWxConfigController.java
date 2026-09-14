package com.example.springboottemplate.controller;

import com.example.springboottemplate.annotation.OperLog;
import com.example.springboottemplate.annotation.OperTypes;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.StoreWxConfigSaveRequest;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.service.StoreWxConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/storeWxConfig")
@Api(tags = "店铺微信配置", description = "买断店小程序与微信支付，仅管理员")
public class StoreWxConfigController {

    @Autowired
    private StoreWxConfigService storeWxConfigService;

    @GetMapping
    @ResponseBody
    @ApiOperation(value = "查询店铺微信配置", notes = "不回传密钥明文")
    public Response get(@RequestParam Long storeId, HttpServletRequest request) {
        try {
            return storeWxConfigService.getByStoreId(storeId, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value = "店铺微信配置列表")
    public Response list(HttpServletRequest request) {
        try {
            return storeWxConfigService.list(request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @PutMapping
    @ResponseBody
    @ApiOperation(value = "保存店铺微信配置", notes = "密钥字段留空表示不修改")
    @OperLog(module = "店铺微信配置", type = OperTypes.UPDATE)
    public Response save(@RequestBody StoreWxConfigSaveRequest body, HttpServletRequest request) {
        try {
            return storeWxConfigService.save(body, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("保存失败: " + e.getMessage());
        }
    }
}
