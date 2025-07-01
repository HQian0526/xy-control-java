package com.example.springboottemplate.service;

import com.example.springboottemplate.entity.Card;
import com.example.springboottemplate.dto.Response;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface CardService {
    Response addCard(Card card, HttpServletRequest request);

    Response findCard(Card card, Integer pageNum, Integer pageSize);

    Response updateCard(Card card);

    Response deleteCard(List<Integer> idList);
}
