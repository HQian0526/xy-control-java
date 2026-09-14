package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.StoreMemberRecharge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface StoreMemberRechargeMapper extends BaseMapper<StoreMemberRecharge> {
    void addRecharge(StoreMemberRecharge recharge);

    StoreMemberRecharge selectByRechargeNo(@Param("rechargeNo") String rechargeNo);

    int markPaid(@Param("rechargeNo") String rechargeNo,
                 @Param("transactionId") String transactionId,
                 @Param("paidTime") Date paidTime);

    int updatePrepayId(@Param("rechargeNo") String rechargeNo, @Param("prepayId") String prepayId);

    int markClosed(@Param("rechargeNo") String rechargeNo, @Param("closedTime") Date closedTime);

    List<StoreMemberRecharge> selectExpiredUnpaid(@Param("expireBefore") Date expireBefore,
                                                  @Param("limit") int limit);

    int markStoreSettled(@Param("storeId") Long storeId,
                         @Param("settleTime") Date settleTime,
                         @Param("settleBy") String settleBy);
}
