package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.Catagory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CatagoryMapper extends BaseMapper<Catagory> {
    void addCatagory(Catagory catagory);  //新增商品分类

    List<Catagory> findCatagory(Catagory catagory); //查找所有商品分类

    void updateCatagory(Catagory catagory); //修改商品分类信息
}
