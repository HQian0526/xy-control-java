package com.example.springboottemplate.mapper.contract;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.contract.Contract;
import com.example.springboottemplate.entity.contract.ContractTemplate;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TemplateMapper extends BaseMapper<ContractTemplate> {
    List<ContractTemplate> findContractTemp(ContractTemplate contractTemplate); //查找所有合同模板
}
