package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.StoreMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface StoreMemberMapper extends BaseMapper<StoreMember> {
    StoreMember selectByStoreAndUser(@Param("storeId") Long storeId, @Param("userId") Long userId);

    int insertMember(StoreMember member);

    int updateMemberStatus(@Param("storeId") Long storeId,
                           @Param("userId") Long userId,
                           @Param("isMember") Integer isMember,
                           @Param("updateTime") java.util.Date updateTime);

    int debitBalance(@Param("storeId") Long storeId,
                     @Param("userId") Long userId,
                     @Param("amount") BigDecimal amount,
                     @Param("updateTime") java.util.Date updateTime);

    int creditBalance(@Param("storeId") Long storeId,
                      @Param("userId") Long userId,
                      @Param("amount") BigDecimal amount,
                      @Param("updateTime") java.util.Date updateTime);

    int addBalanceAndJoin(@Param("storeId") Long storeId,
                          @Param("userId") Long userId,
                          @Param("amount") BigDecimal amount,
                          @Param("updateTime") java.util.Date updateTime);
}
