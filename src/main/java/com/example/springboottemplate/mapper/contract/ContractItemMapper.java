package com.example.springboottemplate.mapper.contract;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.contract.ContractItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ContractItemMapper extends BaseMapper<ContractItem> {
    // 添加批量插入方法
    int insertBatchSomeColumn(@Param("list") List<ContractItem> list);
}
