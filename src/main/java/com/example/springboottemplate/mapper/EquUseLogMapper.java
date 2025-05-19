package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.EquUseLog;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface EquUseLogMapper extends BaseMapper<EquUseLog> {
    void addEquUseLog(EquUseLog equUseLog);  //新增设备使用记录

    List<EquUseLog> findEquUseLog(EquUseLog equUseLog); //查找所有设备使用记录

    void updateEquUseLog(EquUseLog equUseLog); //修改设备使用记录信息
}
