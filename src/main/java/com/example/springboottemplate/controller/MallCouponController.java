package com.example.springboottemplate.controller;

import com.example.springboottemplate.annotation.OperLog;
import com.example.springboottemplate.annotation.OperTypes;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.MallCouponTemplate;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.service.MallCouponService;
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
import java.util.Map;

@Controller
@RequestMapping("/mallCoupon")
@Api(tags = "店铺优惠券", description = "店铺优惠券模板")
public class MallCouponController {

    @Autowired
    private MallCouponService mallCouponService;

    @PostMapping("/addMallCouponTemplate")
    @ResponseBody
    @ApiOperation(value = "新增优惠券")
    @OperLog(module = "优惠券", type = OperTypes.ADD)
    public Response addMallCouponTemplate(@RequestBody MallCouponTemplate record, HttpServletRequest request) {
        try {
            return mallCouponService.addMallCouponTemplate(record, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("新增失败: " + e.getMessage());
        }
    }

    @GetMapping("/findMallCouponTemplate")
    @ResponseBody
    @ApiOperation(value = "查询优惠券")
    public Response findMallCouponTemplate(MallCouponTemplate record, Integer pageNum, Integer pageSize,
                                           HttpServletRequest request) {
        try {
            return mallCouponService.findMallCouponTemplate(record, pageNum, pageSize, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @PutMapping("/updateMallCouponTemplate")
    @ResponseBody
    @ApiOperation(value = "修改优惠券")
    @OperLog(module = "优惠券", type = OperTypes.UPDATE)
    public Response updateMallCouponTemplate(@RequestBody MallCouponTemplate record, HttpServletRequest request) {
        try {
            return mallCouponService.updateMallCouponTemplate(record, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("修改失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/deleteMallCouponTemplate")
    @ResponseBody
    @ApiOperation(value = "删除优惠券")
    @OperLog(module = "优惠券", type = OperTypes.DELETE)
    public Response deleteMallCouponTemplate(@RequestBody JsonNode body, HttpServletRequest request) {
        try {
            List<Long> idList = JsonUtil.parseIdList(body);
            return mallCouponService.deleteMallCouponTemplate(idList, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("删除失败: " + e.getMessage());
        }
    }

    @GetMapping("/storeTemplates")
    @ResponseBody
    @ApiOperation(value = "本店可领优惠券", notes = "游客可看；登录后带 claimed")
    public Response listStoreTemplates(Long storeId, HttpServletRequest request) {
        try {
            return mallCouponService.listStoreTemplates(storeId, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/grant")
    @ResponseBody
    @ApiOperation(value = "商家发放优惠券")
    @OperLog(module = "优惠券", type = OperTypes.ADD)
    public Response grantCoupon(@RequestBody JsonNode body, HttpServletRequest request) {
        try {
            Long templateId = parseLongId(body == null ? null : body.get("templateId"));
            List<Long> userIds = JsonUtil.parseIdList(body == null ? null : body.get("userIds"));
            return mallCouponService.grantCoupon(templateId, userIds, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("发放失败: " + e.getMessage());
        }
    }

    @PostMapping("/receive")
    @ResponseBody
    @ApiOperation(value = "领取优惠券")
    public Response receiveCoupon(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long templateId = parseLongId(body == null ? null : body.get("templateId"));
            return mallCouponService.receiveCoupon(templateId, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("领取失败: " + e.getMessage());
        }
    }

    @GetMapping("/myCoupons")
    @ResponseBody
    @ApiOperation(value = "我的优惠券", notes = "tab: unused / used / expired")
    public Response findMyCoupons(String tab, Long storeId, Integer pageNum, Integer pageSize,
                                  HttpServletRequest request) {
        try {
            return mallCouponService.findMyCoupons(tab, storeId, pageNum, pageSize, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    private Long parseLongId(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof JsonNode) {
            JsonNode node = (JsonNode) raw;
            if (node.isNull() || node.isMissingNode()) {
                return null;
            }
            if (node.isNumber()) {
                return node.longValue();
            }
            raw = node.asText();
        }
        if (raw instanceof Number) {
            return ((Number) raw).longValue();
        }
        String text = String.valueOf(raw).trim();
        if (text.isEmpty() || "null".equals(text)) {
            return null;
        }
        try {
            return Long.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
