package com.example.springboottemplate.serviceimpl.contract;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.example.springboottemplate.dto.contract.ContractDetailVO;
import com.example.springboottemplate.dto.contract.ContractItemVO;
import com.example.springboottemplate.entity.contract.ContractTemplate;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.contract.ContractTemplateMapper;
import com.example.springboottemplate.service.contract.TemplateService;
import com.alibaba.fastjson.JSON;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    private ContractTemplateMapper templateMapper;

    @Value("${template.upload-path}")
    private String templateUploadPath;

    @Override
    public ContractTemplate getTemplateByContractType(Integer contractType) {
        return templateMapper.selectOne(
                new LambdaQueryWrapper<ContractTemplate>()
                        .eq(ContractTemplate::getTemplateType, contractType)
                        .eq(ContractTemplate::getDeleted, 0) // 只查询启用的模板
                        .last("LIMIT 1") // 确保只返回一条记录
        );
    }

    @Override
    public byte[] fillTemplate(Long templateId, ContractDetailVO contractDetailVO) throws Exception {
        ContractTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }

        // 1. 加载模板文件
        File templateFile = new File(templateUploadPath + template.getFilePath());
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(templateFile);

        // 2. 准备数据
        Map<String, String> data = prepareTemplateData(contractDetailVO);

        // 3. 使用docx4j填充变量
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
        VariablePrepare.prepare(wordMLPackage);
        documentPart.variableReplace(data);

        // 4. 生成文档字节数组
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wordMLPackage.save(out);
        return out.toByteArray();
    }

    private Map<String, String> prepareTemplateData(ContractDetailVO contractDetailVO) {
        Map<String, String> data = new HashMap<>();

        // 基础合同信息
        data.put("contractNo", contractDetailVO.getContractNo());
        data.put("contractName", contractDetailVO.getContractName());
        data.put("lessorName", contractDetailVO.getLessorName());
        data.put("lesseeName", contractDetailVO.getLesseeName());

        // 日期格式化
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        data.put("startDate", contractDetailVO.getStartDate().format(dateFormatter));
        data.put("endDate", contractDetailVO.getEndDate().format(dateFormatter));

        // 金额格式化
        data.put("totalAmount", contractDetailVO.getTotalAmount().setScale(2).toString());

        // 合同项列表
        if (CollectionUtils.isNotEmpty(contractDetailVO.getItems())) {
            for (int i = 0; i < contractDetailVO.getItems().size(); i++) {
                ContractItemVO item = contractDetailVO.getItems().get(i);
                data.put("itemName_" + i, item.getItemName());
                data.put("quantity_" + i, String.valueOf(item.getQuantity()));
                // 其他字段...
            }
        }
        return data;
    }

    @Override
    public Map<String, Object> previewTemplateVariables(Long templateId) {
        ContractTemplate template = templateMapper.selectById(templateId);
        if (template == null) throw new BusinessException("模板不存在");

        // 假设 variablesConfig 是 JSON 字段，存储变量定义
        // 格式示例：{"variables":[{"name":"contractNo","source":"contract"}]}
        return JSON.parseObject(template.getVariablesConfig(), Map.class);
    }
}
