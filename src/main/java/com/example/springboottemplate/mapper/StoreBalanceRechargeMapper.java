package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.StoreBalanceRecharge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface StoreBalanceRechargeMapper extends BaseMapper<StoreBalanceRecharge> {
    void addRecharge(StoreBalanceRecharge recharge);

    StoreBalanceRecharge selectByRechargeNo(@Param("rechargeNo") String rechargeNo);

    int markPaid(@Param("rechargeNo") String rechargeNo,
                 @Param("transactionId") String transactionId,
                 @Param("paidTime") Date paidTime);

    int updatePrepayId(@Param("rechargeNo") String rechargeNo, @Param("prepayId") String prepayId);

    int markStoreSettled(@Param("storeId") Long storeId,
                         @Param("settleTime") Date settleTime,
                         @Param("settleBy") String settleBy);

    int updateWxShippingSync(@Param("rechargeNo") String rechargeNo,
                             @Param("status") Integer status,
                             @Param("errcode") Integer errcode,
                             @Param("errmsg") String errmsg,
                             @Param("retry") Integer retry,
                             @Param("shippingTime") Date shippingTime);

    List<StoreBalanceRecharge> selectPendingWxShipping(@Param("maxRetry") int maxRetry,
                                                       @Param("limit") int limit);
}
