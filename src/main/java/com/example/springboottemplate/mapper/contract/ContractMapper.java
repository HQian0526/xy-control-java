package com.example.springboottemplate.mapper.contract;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.contract.Contract;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ContractMapper extends BaseMapper<Contract> {
    void addContract(Contract contract);  //新增合同

    List<Contract> findContract(Contract contract); //查找所有合同

    void updateContract(Contract contract); //修改合同信息

    Contract selectByContractNo(String contractNo);
}
