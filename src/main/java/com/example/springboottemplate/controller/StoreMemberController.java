package com.example.springboottemplate.controller;

import com.example.springboottemplate.annotation.OperLog;
import com.example.springboottemplate.annotation.OperTypes;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.mall.StoreMemberRechargeRequest;
import com.example.springboottemplate.dto.mall.StoreMemberStatusRequest;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.service.StoreMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/storeMember")
@Api(tags = "店铺会员", description = "本店会员储值与身份")
public class StoreMemberController {

    @Autowired
    private StoreMemberService storeMemberService;

    @GetMapping("/mine")
    @ResponseBody
    @ApiOperation(value = "当前用户在本店的会员信息")
    public Response mine(@RequestParam(required = false) Long storeId, HttpServletRequest request) {
        try {
            return storeMemberService.mine(storeId, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/recharge")
    @ResponseBody
    @ApiOperation(value = "会员储值并获取微信支付参数", notes = "每次至少 100 元；支付成功后入本店余额并成为会员")
    @OperLog(module = "店铺会员", type = OperTypes.ADD)
    public Response recharge(@RequestBody StoreMemberRechargeRequest request, HttpServletRequest httpRequest) {
        try {
            return storeMemberService.recharge(request, httpRequest);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("储值下单失败: " + e.getMessage());
        }
    }

    @GetMapping("/recharge/query")
    @ResponseBody
    @ApiOperation(value = "查询储值单")
    public Response queryRecharge(@RequestParam String rechargeNo, HttpServletRequest request) {
        try {
            return storeMemberService.queryRecharge(rechargeNo, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/recharge/mockConfirm")
    @ResponseBody
    @ApiOperation(value = "mock 确认储值支付", notes = "仅 wechat.pay.mock=true 时可用")
    public Response mockConfirmRecharge(@RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            String rechargeNo = body == null ? null : body.get("rechargeNo");
            return storeMemberService.mockConfirmRecharge(rechargeNo, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("确认失败: " + e.getMessage());
        }
    }

    @PutMapping("/status")
    @ResponseBody
    @ApiOperation(value = "修改本店顾客会员身份", notes = "仅基础用户/会员；有余额时不允许降级")
    @OperLog(module = "店铺会员", type = OperTypes.UPDATE)
    public Response updateStatus(@RequestBody StoreMemberStatusRequest request, HttpServletRequest httpRequest) {
        try {
            return storeMemberService.updateStatus(request, httpRequest);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("修改失败: " + e.getMessage());
        }
    }
}
