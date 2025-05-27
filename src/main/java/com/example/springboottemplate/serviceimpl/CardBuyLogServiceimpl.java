package com.example.springboottemplate.serviceimpl;

import com.example.springboottemplate.entity.CardBuyLog;
import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.mapper.CardBuyLogMapper;
import com.example.springboottemplate.service.CardBuyLogService;
import com.example.springboottemplate.utils.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CardBuyLogServiceimpl implements CardBuyLogService {
    @Autowired
    private CardBuyLogMapper cardBuyLogMapper;

    @Override
    public Response addCardBuyLog(CardBuyLog cardBuyLog) {
        cardBuyLogMapper.addCardBuyLog(cardBuyLog);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findCardBuyLog(CardBuyLog cardBuyLog) {
        List<CardBuyLog> list = cardBuyLogMapper.findCardBuyLog(cardBuyLog);
        return new Response(200, list, "操作成功");
    }

    @Override
    public Response updateCardBuyLog(CardBuyLog cardBuyLog) {
        cardBuyLogMapper.updateCardBuyLog(cardBuyLog);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteCardBuyLog(List<Integer> idList) {
        if (ValidateUtil.isEmpty(idList)) {  // 使用工具类
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        int affectedRows = cardBuyLogMapper.deleteBatchIds(idList); // 调用mybatis-plus的逻辑删除，返回受影响行数
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        } else {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
    }
}
