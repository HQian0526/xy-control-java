package com.example.springboottemplate.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.system.OperLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OperLogMapper extends BaseMapper<OperLog> {
    void addOperLog(OperLog operLog);

    List<OperLog> findOperLog(OperLog operLog);

    void updateOperLog(OperLog operLog);
}
