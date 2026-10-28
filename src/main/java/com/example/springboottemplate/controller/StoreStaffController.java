package com.example.springboottemplate.controller;

import com.example.springboottemplate.annotation.OperLog;
import com.example.springboottemplate.annotation.OperTypes;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.StoreStaff;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.service.StoreStaffService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/storeStaff")
@Api(tags = "店铺员工", description = "商家员工绑定与管理")
public class StoreStaffController {

    @Autowired
    private StoreStaffService storeStaffService;

    @PostMapping("/addStoreStaff")
    @ResponseBody
    @ApiOperation(value = "新增员工", notes = "按已注册小程序手机号绑定；一人同时只能在一家店任职")
    @OperLog(module = "员工管理", type = OperTypes.ADD)
    public Response addStoreStaff(@RequestBody StoreStaff record, HttpServletRequest request) {
        try {
            return storeStaffService.addStoreStaff(record, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("新增失败: " + e.getMessage());
        }
    }

    @GetMapping("/findStoreStaff")
    @ResponseBody
    @ApiOperation(value = "查询员工", notes = "商户仅本店；管理员可按 storeId 过滤")
    public Response findStoreStaff(StoreStaff record, Integer pageNum, Integer pageSize, HttpServletRequest request) {
        try {
            return storeStaffService.findStoreStaff(record, pageNum, pageSize, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @PutMapping("/updateStoreStaff")
    @ResponseBody
    @ApiOperation(value = "修改员工", notes = "仅允许改启用状态和备注")
    @OperLog(module = "员工管理", type = OperTypes.UPDATE)
    public Response updateStoreStaff(@RequestBody StoreStaff record, HttpServletRequest request) {
        try {
            return storeStaffService.updateStoreStaff(record, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("修改失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/deleteStoreStaff")
    @ResponseBody
    @ApiOperation(value = "删除员工", notes = "逻辑删除后可改绑其他店")
    @OperLog(module = "员工管理", type = OperTypes.DELETE)
    public Response deleteStoreStaff(@RequestBody List<Long> idList, HttpServletRequest request) {
        try {
            return storeStaffService.deleteStoreStaff(idList, request);
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        } catch (Exception e) {
            return Response.fail("删除失败: " + e.getMessage());
        }
    }
}
