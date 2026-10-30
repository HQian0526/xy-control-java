package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.StoreBalanceAdjust;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@Mapper
public interface StoreBalanceAdjustMapper extends BaseMapper<StoreBalanceAdjust> {
    int insertIgnore(StoreBalanceAdjust adjust);

    int markStoreSettled(@Param("storeId") Long storeId,
                         @Param("settleTime") Date settleTime,
                         @Param("settleBy") String settleBy);
}
