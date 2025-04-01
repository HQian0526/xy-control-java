package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.Store;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StoreMapper extends BaseMapper<Store> {
    void addStore(Store store);  //新增商户

    List<Store> findStore(Store store); //查找所有商户

    void updateStore(Store store); //修改商户信息

    void deleteStore(int id); //删除商户信息
}
