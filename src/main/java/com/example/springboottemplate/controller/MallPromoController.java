package com.example.springboottemplate.controller;

import com.example.springboottemplate.annotation.OperLog;
import com.example.springboottemplate.annotation.OperTypes;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.MallPromo;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.service.MallPromoService;
import com.example.springboottemplate.utils.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/mallPromo")
@Api(tags = "店铺满减", description = "店铺满减活动")
public class MallPromoController {

    @Autowired
    private MallPromoService mallPromoService;

    @PostMapping("/addMallPromo")
    @ResponseBody
    @ApiOperation(value = "新增满减活动")
    @OperLog(module = "满减活动", type = OperTypes.ADD)
    public Response addMallPromo(@RequestBody MallPromo record, HttpServletRequest request) {
        try {
            return mallPromoService.addMallPromo(record, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("新增失败: " + e.getMessage());
        }
    }

    @GetMapping("/findMallPromo")
    @ResponseBody
    @ApiOperation(value = "查询满减活动")
    public Response findMallPromo(MallPromo record, Integer pageNum, Integer pageSize, HttpServletRequest request) {
        try {
            return mallPromoService.findMallPromo(record, pageNum, pageSize, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @PutMapping("/updateMallPromo")
    @ResponseBody
    @ApiOperation(value = "修改满减活动")
    @OperLog(module = "满减活动", type = OperTypes.UPDATE)
    public Response updateMallPromo(@RequestBody MallPromo record, HttpServletRequest request) {
        try {
            return mallPromoService.updateMallPromo(record, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("修改失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/deleteMallPromo")
    @ResponseBody
    @ApiOperation(value = "删除满减活动")
    @OperLog(module = "满减活动", type = OperTypes.DELETE)
    public Response deleteMallPromo(@RequestBody JsonNode body, HttpServletRequest request) {
        try {
            List<Long> idList = JsonUtil.parseIdList(body);
            return mallPromoService.deleteMallPromo(idList, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("删除失败: " + e.getMessage());
        }
    }

    @GetMapping("/active")
    @ResponseBody
    @ApiOperation(value = "本店进行中满减", notes = "游客可看")
    public Response getActivePromo(Long storeId) {
        try {
            return mallPromoService.getActivePromo(storeId);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }
}
