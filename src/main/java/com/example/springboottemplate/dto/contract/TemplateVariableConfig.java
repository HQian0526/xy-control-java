package com.example.springboottemplate.dto.contract;

import lombok.Data;

import java.util.List;

@Data
public class TemplateVariableConfig {
    private List<TemplateVariable> variables;

    @Data
    public static class TemplateVariable {
        private String variableName;
        private String displayName;
        private String dataSource; // 数据来源: contract, item, custom等
        private String fieldPath; // 字段路径: contract.contractName, items[].itemName等
    }
}
