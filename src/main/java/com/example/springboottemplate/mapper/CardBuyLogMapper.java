package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.CardBuyLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CardBuyLogMapper extends BaseMapper<CardBuyLog> {
    void addCardBuyLog(CardBuyLog cardBuyLog);  //新增套餐购买记录

    List<CardBuyLog> findCardBuyLog(CardBuyLog cardBuyLog); //查找所有套餐购买记录

    void updateCardBuyLog(CardBuyLog cardBuyLog); //修改套餐购买记录信息
}
