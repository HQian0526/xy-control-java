package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.OtherBusiness;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OtherBusinessMapper extends BaseMapper<OtherBusiness> {
    void addOtherBusiness(OtherBusiness otherBusiness);

    List<OtherBusiness> findOtherBusiness(OtherBusiness otherBusiness);

    void updateOtherBusiness(OtherBusiness otherBusiness);
}
