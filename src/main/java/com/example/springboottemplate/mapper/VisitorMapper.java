package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.Visitor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VisitorMapper extends BaseMapper<Visitor> {
    void addVisitor(Visitor visitor);

    List<Visitor> findVisitor(Visitor visitor);

    void updateVisitor(Visitor visitor);
}
