package com.example.springboottemplate.controller;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.StoreVerifyLog;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.service.StoreVerifyLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/storeVerifyLog")
@Api(tags = "核验记录", description = "店铺会员核验扣款流水")
public class StoreVerifyLogController {

    @Autowired
    private StoreVerifyLogService storeVerifyLogService;

    @GetMapping("/findStoreVerifyLog")
    @ResponseBody
    @ApiOperation(value = "查询核验记录", notes = "商户仅本店；记录操作员工/商家")
    public Response findStoreVerifyLog(StoreVerifyLog record, Integer pageNum, Integer pageSize,
                                       HttpServletRequest request) {
        try {
            return storeVerifyLogService.findStoreVerifyLog(record, pageNum, pageSize, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }
}
