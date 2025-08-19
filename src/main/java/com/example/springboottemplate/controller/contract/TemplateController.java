package com.example.springboottemplate.controller.contract;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.contract.ContractDTO;
import com.example.springboottemplate.dto.contract.ContractDetailVO;
import com.example.springboottemplate.entity.contract.ContractTemplate;
import com.example.springboottemplate.service.contract.ContractService;
import com.example.springboottemplate.service.contract.TemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/contract-template")
@Api(tags = "合同模板管理", description = "合同模板相关接口")
public class TemplateController {
    @Autowired
    private TemplateService templateService;

    @Autowired
    private ContractService contractService;

    @GetMapping("/preview/{contractNo}")
    @ApiOperation("预览合同文档")
    public ResponseEntity<byte[]> previewContract(@PathVariable String contractNo) throws Exception {
        // 1. 获取合同数据
        ContractDetailVO contractDetailVO = contractService.getContractDetail(contractNo);

        // 2. 根据合同类型获取模板
        ContractTemplate template = templateService.getTemplateByContractType(contractDetailVO.getContractType());

        // 3. 填充模板生成文档
        byte[] documentBytes = templateService.fillTemplate(template.getId(), contractDetailVO);

        // 4. 返回文档流
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "contract_" + contractNo + ".docx");
        return new ResponseEntity<>(documentBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/variables/{templateId}")
    @ApiOperation("获取模板变量配置")
    public Response getTemplateVariables(@PathVariable Long templateId) {
        return Response.success(templateService.previewTemplateVariables(templateId));
    }
}
