package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.StoreStaff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StoreStaffMapper extends BaseMapper<StoreStaff> {
    void addStoreStaff(StoreStaff record);

    List<StoreStaff> findStoreStaff(StoreStaff record);

    void updateStoreStaff(StoreStaff record);

    StoreStaff selectEnabledByUserId(@Param("userId") Long userId);

    StoreStaff selectByUserId(@Param("userId") Long userId);
}
