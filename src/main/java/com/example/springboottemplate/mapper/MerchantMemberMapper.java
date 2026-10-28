package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.MerchantMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;

@Mapper
public interface MerchantMemberMapper extends BaseMapper<MerchantMember> {
    MerchantMember selectByMerchantAndUser(@Param("merchantUserId") Long merchantUserId,
                                           @Param("userId") Long userId);

    int insertMember(MerchantMember member);

    int updateMemberStatus(@Param("merchantUserId") Long merchantUserId,
                           @Param("userId") Long userId,
                           @Param("isMember") Integer isMember,
                           @Param("updateTime") Date updateTime);

    int debitBalance(@Param("merchantUserId") Long merchantUserId,
                     @Param("userId") Long userId,
                     @Param("amount") BigDecimal amount,
                     @Param("updateTime") Date updateTime);

    int creditBalance(@Param("merchantUserId") Long merchantUserId,
                      @Param("userId") Long userId,
                      @Param("amount") BigDecimal amount,
                      @Param("updateTime") Date updateTime);

    int addBalanceAndJoin(@Param("merchantUserId") Long merchantUserId,
                          @Param("userId") Long userId,
                          @Param("amount") BigDecimal amount,
                          @Param("updateTime") Date updateTime);
}
