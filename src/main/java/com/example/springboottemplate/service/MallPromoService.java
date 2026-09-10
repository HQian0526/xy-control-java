package com.example.springboottemplate.service;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.MallPromo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface MallPromoService {
    Response addMallPromo(MallPromo record, HttpServletRequest request);

    Response findMallPromo(MallPromo record, Integer pageNum, Integer pageSize, HttpServletRequest request);

    Response updateMallPromo(MallPromo record, HttpServletRequest request);

    Response deleteMallPromo(List<Long> idList, HttpServletRequest request);

    MallPromo findActiveByStoreId(Long storeId);

    Response getActivePromo(Long storeId);
}
