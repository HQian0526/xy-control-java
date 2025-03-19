package com.example.springboottemplate.mapper;

import com.example.springboottemplate.entity.Store;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StoreMapper {
    public void addStore(Store store);  //新增商户

    public List<Store> findStore(Store store); //查找所有商户

    public void updateStore(Store store); //修改商户信息

    public void deleteStore(int id); //删除商户信息
}
