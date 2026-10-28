package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.StoreVerifyLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StoreVerifyLogMapper extends BaseMapper<StoreVerifyLog> {
    void addStoreVerifyLog(StoreVerifyLog record);

    List<StoreVerifyLog> findStoreVerifyLog(StoreVerifyLog record);
}
