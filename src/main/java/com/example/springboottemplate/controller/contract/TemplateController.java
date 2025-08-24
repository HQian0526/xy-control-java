package com.example.springboottemplate.controller.contract;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.contract.ContractDTO;
import com.example.springboottemplate.dto.contract.ContractDetailVO;
import com.example.springboottemplate.entity.contract.Contract;
import com.example.springboottemplate.entity.contract.ContractTemplate;
import com.example.springboottemplate.service.contract.ContractService;
import com.example.springboottemplate.service.contract.TemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/contract-template")
@Api(tags = "合同模板管理", description = "合同模板相关接口")
public class TemplateController {
    @Autowired
    private TemplateService templateService;

    @Autowired
    private ContractService contractService;

    // 新增合同模板
    @PostMapping("/addContractTemp")
    @ResponseBody
    @ApiOperation(value = "添加合同模板", notes = "传入合同各项信息进行添加合同模板")
    public Response addContractTemp(@RequestBody ContractTemplate contractTemplate, HttpServletRequest request){
        return templateService.addContractTemp(contractTemplate, request);
    }

    // 查询合同模板
    @GetMapping("/findContractTemp")
    @ResponseBody
    @ApiOperation(value = "查询合同模板", notes = "查询合同表中所有合同模板")
    public Response findContractTemp(ContractTemplate contractTemplate, Integer pageNum, Integer pageSize){
        return templateService.findContractTemp(contractTemplate, pageNum, pageSize);
    }

    //修改合同模板信息
    @PutMapping("/updateContractTemp")
    @ResponseBody
    @ApiOperation(value = "修改合同模板信息", notes = "根据id更新合同模板信息")
    public Response updateContractTemp(@RequestBody ContractTemplate contractTemplate, HttpServletRequest request){
        return templateService.updateContractTemp(contractTemplate, request);
    }

    //删除合同模板信息
    @DeleteMapping("/deleteContractTemp")
    @ResponseBody
    @ApiOperation(value = "删除合同模板", notes = "根据id删除合同模板")
    public Response deleteContractTemp(@RequestBody List<Integer> idList){
        return templateService.deleteContractTemp(idList);
    }

    @GetMapping("/preview/{contractNo}")
    @ApiOperation("预览合同文档")
    public ResponseEntity<byte[]> previewContract(@PathVariable String contractNo) throws Exception {
        // 1. 获取合同数据
        ContractDetailVO contractDetailVO = contractService.getContractDetail(contractNo);

        // 2. 根据合同类型获取模板
        ContractTemplate template = templateService.getTemplateByContractType(contractDetailVO.getContractType());

        // 3. 填充模板生成文档
        byte[] documentBytes = templateService.fillTemplate(template.getTemplateNo(), contractDetailVO);

        // 4. 返回文档流
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "contract_" + contractNo + ".docx");
        return new ResponseEntity<>(documentBytes, headers, HttpStatus.OK);
    }

    /**
     * 预览合同模板
     * @param templateNo 模板编号
     * @return 模板文件
     */
    @GetMapping("/previewTemp/{templateNo}")
    public ResponseEntity<?> previewTemplate(@PathVariable String templateNo) {
        return templateService.previewTemplate(templateNo);
    }

    @GetMapping("/variables/{templateNo}")
    @ApiOperation("获取模板变量配置")
    public Response getTemplateVariables(@PathVariable String templateNo) {
        return Response.success(templateService.previewTemplateVariables(templateNo));
    }
}
