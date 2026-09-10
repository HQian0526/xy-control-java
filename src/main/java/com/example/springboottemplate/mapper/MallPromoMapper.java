package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.MallPromo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface MallPromoMapper extends BaseMapper<MallPromo> {
    void addMallPromo(MallPromo record);

    List<MallPromo> findMallPromo(MallPromo record);

    MallPromo selectPromoById(@Param("id") Long id);

    void updateMallPromo(MallPromo record);

    MallPromo selectActiveByStoreId(@Param("storeId") Long storeId, @Param("now") Date now);

    int countActiveByStoreId(@Param("storeId") Long storeId,
                             @Param("excludeId") Long excludeId,
                             @Param("now") Date now);
}
