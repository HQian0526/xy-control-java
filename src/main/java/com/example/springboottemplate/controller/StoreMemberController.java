package com.example.springboottemplate.controller;

import com.example.springboottemplate.annotation.OperLog;
import com.example.springboottemplate.annotation.OperTypes;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.mall.StoreMemberAssistRechargeRequest;
import com.example.springboottemplate.dto.mall.StoreMemberRechargeRequest;
import com.example.springboottemplate.dto.mall.StoreMemberStatusRequest;
import com.example.springboottemplate.dto.mall.StoreMemberVerifyDebitRequest;
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

    @GetMapping("/lookup")
    @ResponseBody
    @ApiOperation(value = "按手机号查询本店顾客昵称和储值余额", notes = "仅商家/管理员")
    public Response lookup(@RequestParam(required = false) String phone,
                           @RequestParam(required = false) Long userId,
                           @RequestParam(required = false) Long storeId,
                           HttpServletRequest request) {
        try {
            return storeMemberService.lookupCustomer(phone, userId, storeId, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/assistRecharge")
    @ResponseBody
    @ApiOperation(value = "商家代顾客储值", notes = "入本店余额并成为储值会员；仅商家/管理员")
    @OperLog(module = "店铺会员", type = OperTypes.ADD, remark = "商家代储值")
    public Response assistRecharge(@RequestBody StoreMemberAssistRechargeRequest request, HttpServletRequest httpRequest) {
        try {
            return storeMemberService.assistRecharge(request, httpRequest);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("储值失败: " + e.getMessage());
        }
    }

    @PostMapping("/verifyDebit")
    @ResponseBody
    @ApiOperation(value = "核验扣减本店顾客储值余额", notes = "仅商家/管理员；余额不足时提示")
    @OperLog(module = "店铺会员", type = OperTypes.UPDATE, remark = "核验扣减储值")
    public Response verifyDebit(@RequestBody StoreMemberVerifyDebitRequest request, HttpServletRequest httpRequest) {
        try {
            return storeMemberService.verifyDebit(request, httpRequest);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("核验失败: " + e.getMessage());
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
