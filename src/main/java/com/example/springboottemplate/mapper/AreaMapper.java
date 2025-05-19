package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.Area;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AreaMapper extends BaseMapper<Area> {
    void addArea(Area area);  //新增区域

    List<Area> findArea(Area area); //查找所有区域

    void updateArea(Area area); //修改区域信息
}
