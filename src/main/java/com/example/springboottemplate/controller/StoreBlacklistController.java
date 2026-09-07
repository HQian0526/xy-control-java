package com.example.springboottemplate.controller;

import com.example.springboottemplate.annotation.OperLog;
import com.example.springboottemplate.annotation.OperTypes;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.StoreBlacklist;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.service.StoreBlacklistService;
import com.example.springboottemplate.utils.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/storeBlacklist")
@Api(tags = "店铺黑名单", description = "店铺拉黑顾客相关接口")
public class StoreBlacklistController {

    @Autowired
    private StoreBlacklistService storeBlacklistService;

    @PostMapping("/addStoreBlacklist")
    @ResponseBody
    @ApiOperation(value = "新增黑名单", notes = "可传 userId 或 phone；商户仅能拉黑本店")
    @OperLog(module = "店铺黑名单", type = OperTypes.ADD)
    public Response addStoreBlacklist(@RequestBody StoreBlacklist record, HttpServletRequest request) {
        try {
            return storeBlacklistService.addStoreBlacklist(record, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("新增失败: " + e.getMessage());
        }
    }

    @GetMapping("/findStoreBlacklist")
    @ResponseBody
    @ApiOperation(value = "查询黑名单", notes = "商户仅本店；管理员可按 storeId 过滤")
    public Response findStoreBlacklist(StoreBlacklist record, Integer pageNum, Integer pageSize,
                                       HttpServletRequest request) {
        try {
            return storeBlacklistService.findStoreBlacklist(record, pageNum, pageSize, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @PutMapping("/updateStoreBlacklist")
    @ResponseBody
    @ApiOperation(value = "修改黑名单", notes = "仅允许修改姓名/原因/备注，不可改店铺和手机号")
    @OperLog(module = "店铺黑名单", type = OperTypes.UPDATE)
    public Response updateStoreBlacklist(@RequestBody StoreBlacklist record, HttpServletRequest request) {
        try {
            return storeBlacklistService.updateStoreBlacklist(record, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("修改失败: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/deleteStoreBlacklist", method = {RequestMethod.DELETE, RequestMethod.POST})
    @ResponseBody
    @ApiOperation(value = "解除拉黑", notes = "根据id列表逻辑删除；兼容 JSON 数组与微信小程序把数组转成的对象")
    @OperLog(module = "店铺黑名单", type = OperTypes.DELETE)
    public Response deleteStoreBlacklist(@RequestBody JsonNode body, HttpServletRequest request) {
        try {
            List<Long> idList = JsonUtil.parseIdList(body);
            return storeBlacklistService.deleteStoreBlacklist(idList, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("删除失败: " + e.getMessage());
        }
    }
}
