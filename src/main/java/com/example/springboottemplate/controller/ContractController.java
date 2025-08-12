package com.example.springboottemplate.controller;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.contract.ContractDTO;
import com.example.springboottemplate.dto.contract.ContractQueryDTO;
import com.example.springboottemplate.entity.contract.Contract;
import com.example.springboottemplate.service.contract.ContractService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/contract")
@Api(tags = "合同管理", description = "合同相关接口")
public class ContractController {
    @Autowired
    private ContractService contractService;

    // 新增合同
    @PostMapping("/addContract")
    @ResponseBody
    @ApiOperation(value = "添加合同", notes = "传入合同各项信息进行添加合同")
    public Response addContract(@RequestBody Contract contract, HttpServletRequest request){
        return contractService.addContract(contract, request);
    }

    //查询所有合同
    @GetMapping("/findContract")
    @ResponseBody
    @ApiOperation(value = "查询所有合同", notes = "查询合同表中所有合同")
    public Response findContract(Contract contract, Integer pageNum, Integer pageSize){
        return contractService.findContract(contract, pageNum, pageSize);
    }

    //修改合同信息
    @PutMapping("/updateContract")
    @ResponseBody
    @ApiOperation(value = "修改合同信息", notes = "根据id更新合同信息")
    public Response updateContract(@RequestBody Contract contract, HttpServletRequest request){
        return contractService.updateContract(contract, request);
    }

    //删除合同信息
    @DeleteMapping("/deleteContract")
    @ResponseBody
    @ApiOperation(value = "删除合同", notes = "根据id删除合同")
    public Response deleteContract(@RequestBody List<Integer> idList){
        return contractService.deleteContract(idList);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询合同列表")
    public Response queryContractPage(ContractQueryDTO queryDTO) {
        return Response.success(contractService.queryContractPage(queryDTO));
    }

    @GetMapping("/{no}")
    @ApiOperation("获取合同详情")
    public Response getContractDetail(@PathVariable String no) {
        return Response.success(contractService.getContractDetail(no));
    }

    @PostMapping("/saveContractItems")
    @ResponseBody
    @ApiOperation("保存合同细则")
    public Response saveContract(@RequestBody ContractDTO contractDTO, HttpServletRequest request) {
        return Response.success(contractService.saveContract(contractDTO, request));
    }

    @PostMapping("/terminate/{id}")
    @ResponseBody
    @ApiOperation("终止合同")
    public Response terminateContract(@PathVariable Long id, @RequestParam String reason) {
        return Response.success(contractService.terminateContract(id, reason));
    }
}
