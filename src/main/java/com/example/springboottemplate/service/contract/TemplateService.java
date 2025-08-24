package com.example.springboottemplate.service.contract;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.contract.ContractDetailVO;
import com.example.springboottemplate.entity.contract.Contract;
import com.example.springboottemplate.entity.contract.ContractTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface TemplateService extends IService<ContractTemplate> {
    Response addContractTemp(ContractTemplate contractTemplate, HttpServletRequest request);
    Response findContractTemp(ContractTemplate contractTemplate, Integer pageNum, Integer pageSize);
    Response updateContractTemp(ContractTemplate contractTemplate, HttpServletRequest request);
    Response deleteContractTemp(List<Integer> idList);
    // 根据合同类型获取模板
    ContractTemplate getTemplateByContractType(Integer contractType);

    // 根据合同数据填充模板
    byte[] fillTemplate(String templateNo, ContractDetailVO contractDetailVO) throws Exception;

    // 预览模板变量
    Map<String, Object> previewTemplateVariables(String templateNo);

    /**
     * 预览合同模板
     * @param templateNo 模板编号
     * @return 模板文件资源
     */
    ResponseEntity<?> previewTemplate(String templateNo);
}