package com.example.springboottemplate.controller;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.OtherBusiness;
import com.example.springboottemplate.service.OtherBusinessService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/otherBusiness")
@Api(tags = "其他业务管理", description = "其他业务相关接口")
public class OtherBusinessController {
    @Autowired
    private OtherBusinessService otherBusinessService;

    @PostMapping("/addOtherBusiness")
    @ResponseBody
    @ApiOperation(value = "添加其他业务", notes = "传入业务各项信息进行添加")
    public Response addOtherBusiness(@RequestBody OtherBusiness otherBusiness, HttpServletRequest request) {
        return otherBusinessService.addOtherBusiness(otherBusiness, request);
    }

    @GetMapping("/findOtherBusiness")
    @ResponseBody
    @ApiOperation(value = "查询其他业务", notes = "普通用户按 storeId（或用户 bindStoreId）查；商户仅本店；管理员可按 storeId 过滤")
    public Response findOtherBusiness(OtherBusiness otherBusiness,
                                      @RequestParam(required = false) Long storeId,
                                      Integer pageNum, Integer pageSize,
                                      HttpServletRequest request) {
        // 显式接收 query 的 storeId，避免仅依赖对象绑定时偶发丢失
        if (storeId != null) {
            otherBusiness.setStoreId(storeId);
        }
        return otherBusinessService.findOtherBusiness(otherBusiness, pageNum, pageSize, request);
    }

    @PutMapping("/updateOtherBusiness")
    @ResponseBody
    @ApiOperation(value = "修改其他业务", notes = "根据id更新业务信息")
    public Response updateOtherBusiness(@RequestBody OtherBusiness otherBusiness, HttpServletRequest request) {
        return otherBusinessService.updateOtherBusiness(otherBusiness, request);
    }

    @DeleteMapping("/deleteOtherBusiness")
    @ResponseBody
    @ApiOperation(value = "删除其他业务", notes = "根据id删除业务（逻辑删除）")
    public Response deleteOtherBusiness(@RequestBody List<Long> idList) {
        return otherBusinessService.deleteOtherBusiness(idList);
    }
}
