package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.StoreBlacklist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StoreBlacklistMapper extends BaseMapper<StoreBlacklist> {
    void addStoreBlacklist(StoreBlacklist record);

    List<StoreBlacklist> findStoreBlacklist(StoreBlacklist record);

    void updateStoreBlacklist(StoreBlacklist record);

    StoreBlacklist selectByStoreAndPhone(@Param("storeId") Long storeId, @Param("phone") String phone);

    StoreBlacklist selectByStoreAndPhoneAny(@Param("storeId") Long storeId, @Param("phone") String phone);

    StoreBlacklist selectByStoreAndUserIdAny(@Param("storeId") Long storeId, @Param("userId") Long userId);

    int countHit(@Param("storeId") Long storeId,
                 @Param("phone") String phone,
                 @Param("contact") String contact,
                 @Param("userId") Long userId);
}
