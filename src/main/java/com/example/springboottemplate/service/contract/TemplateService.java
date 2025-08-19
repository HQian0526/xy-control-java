package com.example.springboottemplate.service.contract;

import com.example.springboottemplate.dto.contract.ContractDTO;
import com.example.springboottemplate.dto.contract.ContractDetailVO;
import com.example.springboottemplate.entity.contract.ContractTemplate;

import java.util.Map;

public interface TemplateService {
    // 根据合同类型获取模板
    ContractTemplate getTemplateByContractType(Integer contractType);

    // 根据合同数据填充模板
    byte[] fillTemplate(Long templateId, ContractDetailVO contractDetailVO) throws Exception;

    // 预览模板变量
    Map<String, Object> previewTemplateVariables(Long templateId);
}
