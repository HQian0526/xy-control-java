package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.Equ;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EquMapper extends BaseMapper<Equ> {
    void addEqu(Equ equ);  //新增设备

    List<Equ> findEqu(Equ equ); //查找所有设备

    void updateEqu(Equ equ); //修改设备信息
}
