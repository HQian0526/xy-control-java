package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.Store;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StoreMapper extends BaseMapper<Store> {
    void addStore(Store store);  //新增商户

    List findStore(Store store); //查找所有商户

    void updateStore(Store store); //修改商户信息

    Store selectByStoreId (Long storeId); //根据商户id查询商户
}
