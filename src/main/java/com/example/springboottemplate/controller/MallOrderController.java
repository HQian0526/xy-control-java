package com.example.springboottemplate.controller;

import com.example.springboottemplate.annotation.OperLog;
import com.example.springboottemplate.annotation.OperTypes;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.mall.MallCheckoutRequest;
import com.example.springboottemplate.dto.mall.MallRefundRequest;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.service.MallOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/mallOrder")
@Api(tags = "商城订单", description = "商城结算与微信支付")
public class MallOrderController {

    @Autowired
    private MallOrderService mallOrderService;

    @PostMapping("/checkoutAndPay")
    @ResponseBody
    @ApiOperation(value = "商城下单并获取支付参数", notes = "服务端计价后调微信下单；mock 模式返回 mock=true")
    @OperLog(module = "商城订单", type = OperTypes.ADD)
    public Response checkoutAndPay(@RequestBody MallCheckoutRequest request, HttpServletRequest httpRequest) {
        try {
            return mallOrderService.checkoutAndPay(request, httpRequest);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("下单失败: " + e.getMessage());
        }
    }

    @PostMapping("/previewCheckout")
    @ResponseBody
    @ApiOperation(value = "结算预览", notes = "与下单同一计价，不建单；选中券无效时清空并返回 couponError")
    public Response previewCheckout(@RequestBody MallCheckoutRequest request, HttpServletRequest httpRequest) {
        try {
            return mallOrderService.previewCheckout(request, httpRequest);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("预览失败: " + e.getMessage());
        }
    }

    @GetMapping("/queryOrder")
    @ResponseBody
    @ApiOperation(value = "查询商城订单", notes = "支付后查单；非 mock 时会向微信二次确认")
    public Response queryOrder(@RequestParam String orderNo, HttpServletRequest httpRequest) {
        try {
            return mallOrderService.queryOrder(orderNo, httpRequest);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/findMallOrder")
    @ResponseBody
    @ApiOperation(value = "商城订单列表", notes = "普通用户查本人；商户查本店；管理员可查全部并按 storeId/payStatus 过滤。payStatuses 逗号分隔，如 1,3,4,5")
    public Response findMallOrder(@RequestParam(required = false) Integer payStatus,
                                  @RequestParam(required = false) String payStatuses,
                                  @RequestParam(required = false) Long storeId,
                                  @RequestParam(required = false) Integer pageNum,
                                  @RequestParam(required = false) Integer pageSize,
                                  HttpServletRequest httpRequest) {
        try {
            return mallOrderService.findMallOrder(payStatus, parsePayStatuses(payStatuses),
                    storeId, pageNum, pageSize, httpRequest);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/mockConfirmPay")
    @ResponseBody
    @ApiOperation(value = "mock 确认支付", notes = "仅 wechat.pay.mock=true 时可用")
    @OperLog(module = "商城订单", type = OperTypes.ADD)
    public Response mockConfirmPay(@RequestBody Map<String, String> body, HttpServletRequest httpRequest) {
        try {
            String orderNo = body == null ? null : body.get("orderNo");
            return mallOrderService.mockConfirmPay(orderNo, httpRequest);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("确认支付失败: " + e.getMessage());
        }
    }

    @PostMapping(value = "/wxPayNotify", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "微信支付回调", notes = "微信服务器回调，无需登录")
    public String wxPayNotify(HttpServletRequest request) {
        try {
            String body;
            try (BufferedReader reader = request.getReader()) {
                body = reader.lines().collect(Collectors.joining("\n"));
            }
            return mallOrderService.handleWxPayNotify(request, body);
        } catch (Exception e) {
            return "{\"code\":\"FAIL\",\"message\":\"notify error\"}";
        }
    }

    @PostMapping("/refund")
    @ResponseBody
    @ApiOperation(value = "商户退款", notes = "本店商户或管理员对已支付订单发起全额/部分退款")
    @OperLog(module = "商城订单", type = OperTypes.UPDATE, remark = "订单退款")
    public Response refund(@RequestBody MallRefundRequest request, HttpServletRequest httpRequest) {
        try {
            return mallOrderService.refund(request, httpRequest);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("退款失败: " + e.getMessage());
        }
    }

    @PostMapping(value = "/wxRefundNotify", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "微信退款回调", notes = "微信服务器回调，无需登录")
    public String wxRefundNotify(HttpServletRequest request) {
        try {
            String body;
            try (BufferedReader reader = request.getReader()) {
                body = reader.lines().collect(Collectors.joining("\n"));
            }
            return mallOrderService.handleWxRefundNotify(request, body);
        } catch (Exception e) {
            return "{\"code\":\"FAIL\",\"message\":\"refund notify error\"}";
        }
    }

    @GetMapping("/incomeFlow")
    @ResponseBody
    @ApiOperation(value = "店铺订单金额流水", notes = "按年/季/月/日聚合已支付订单；商家查本店，管理员须传 storeId")
    public Response incomeFlow(@RequestParam(required = false) Long storeId,
                               @RequestParam(required = false, defaultValue = "month") String periodType,
                               @RequestParam(required = false) Integer year,
                               @RequestParam(required = false) Integer yearFrom,
                               @RequestParam(required = false) Integer yearTo,
                               @RequestParam(required = false) Integer month,
                               HttpServletRequest httpRequest) {
        try {
            return mallOrderService.incomeFlow(storeId, periodType, year, yearFrom, yearTo, month, httpRequest);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/financeLedger")
    @ResponseBody
    @ApiOperation(value = "店铺资金明细", notes = "每笔已支付订单记收入，有退款再记一笔退款；商家查本店，管理员须传 storeId")
    public Response financeLedger(@RequestParam(required = false) Long storeId,
                                  @RequestParam(required = false, defaultValue = "all") String type,
                                  @RequestParam(required = false) String date,
                                  @RequestParam(required = false) Integer pageNum,
                                  @RequestParam(required = false) Integer pageSize,
                                  HttpServletRequest httpRequest) {
        try {
            return mallOrderService.financeLedger(storeId, type, date, pageNum, pageSize, httpRequest);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    private List<Integer> parsePayStatuses(String payStatuses) {
        if (payStatuses == null || payStatuses.isBlank()) {
            return null;
        }
        List<Integer> list = new ArrayList<>();
        for (String part : payStatuses.split(",")) {
            if (part == null) {
                continue;
            }
            String value = part.trim();
            if (value.isEmpty()) {
                continue;
            }
            try {
                list.add(Integer.parseInt(value));
            } catch (NumberFormatException ignored) {
                // skip invalid token
            }
        }
        return list.isEmpty() ? null : list;
    }
}
