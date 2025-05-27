package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.Card;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CardMapper extends BaseMapper<Card> {
    void addCard(Card card);  //新增卡

    List<Card> findCard(Card card); //查找所有卡

    void updateCard(Card card); //修改卡信息
}
