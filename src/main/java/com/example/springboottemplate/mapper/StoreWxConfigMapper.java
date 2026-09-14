package com.example.springboottemplate.mapper;

import com.example.springboottemplate.entity.StoreWxConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StoreWxConfigMapper {
    void addStoreWxConfig(StoreWxConfig record);

    void updateStoreWxConfig(StoreWxConfig record);

    StoreWxConfig selectByStoreId(Long storeId);

    StoreWxConfig selectByAppId(String appId);

    StoreWxConfig selectAnyByAppId(String appId);

    StoreWxConfig selectByMchId(String mchId);

    StoreWxConfig selectAnyByMchId(String mchId);

    List<StoreWxConfig> findStoreWxConfig(StoreWxConfig query);
}
