package com.example.springboottemplate.controller;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.service.DashboardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/dashboard")
@Api(tags = "首页概览", description = "经营数据统计")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/overview")
    @ResponseBody
    @ApiOperation(value = "首页经营概览", notes = "商家看当前门店；管理员看全平台。销售额仅统计微信/余额实收净额，不含无需支付的预定单")
    public Response overview(HttpServletRequest request) {
        try {
            return dashboardService.overview(request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }
}
