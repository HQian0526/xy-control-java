package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.MallOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MallOrderMapper extends BaseMapper<MallOrder> {
    void addMallOrder(MallOrder order);

    MallOrder selectByOrderNo(@Param("orderNo") String orderNo);

    java.util.List<MallOrder> findMallOrder(MallOrder order);

    int markPaid(@Param("orderNo") String orderNo,
                 @Param("transactionId") String transactionId,
                 @Param("paidTime") java.util.Date paidTime);

    int updatePrepayId(@Param("orderNo") String orderNo, @Param("prepayId") String prepayId);
}
