package com.example.springboottemplate.service;

import com.example.springboottemplate.entity.CardBuyLog;
import com.example.springboottemplate.entity.Response;

import java.util.List;

public interface CardBuyLogService {
    Response addCardBuyLog(CardBuyLog cardBuyLog);

    Response findCardBuyLog(CardBuyLog cardBuyLog);

    Response updateCardBuyLog(CardBuyLog cardBuyLog);

    Response deleteCardBuyLog(List<Integer> idList);
}
