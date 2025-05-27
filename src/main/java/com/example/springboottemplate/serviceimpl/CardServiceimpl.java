package com.example.springboottemplate.serviceimpl;

import com.example.springboottemplate.entity.Card;
import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.mapper.CardMapper;
import com.example.springboottemplate.service.CardService;
import com.example.springboottemplate.utils.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CardServiceimpl implements CardService {
    @Autowired
    private CardMapper cardMapper;

    @Override
    public Response addCard(Card card) {
        cardMapper.addCard(card);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findCard(Card card) {
        List<Card> list = cardMapper.findCard(card);
        return new Response(200, list, "操作成功");
    }

    @Override
    public Response updateCard(Card card) {
        cardMapper.updateCard(card);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteCard(List<Integer> idList) {
        if (ValidateUtil.isEmpty(idList)) {  // 使用工具类
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        int affectedRows = cardMapper.deleteBatchIds(idList); // 调用mybatis-plus的逻辑删除，返回受影响行数
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        } else {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
    }
}
