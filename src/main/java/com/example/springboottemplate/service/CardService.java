package com.example.springboottemplate.service;

import com.example.springboottemplate.entity.Card;
import com.example.springboottemplate.entity.Response;

import java.util.List;

public interface CardService {
    Response addCard(Card card);

    Response findCard(Card card, Integer pageNum, Integer pageSize);

    Response updateCard(Card card);

    Response deleteCard(List<Integer> idList);
}
