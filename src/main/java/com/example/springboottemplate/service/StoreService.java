package com.example.springboottemplate.service;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.Store;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface StoreService {
    Response addStore(Store store, HttpServletRequest request);

    Response findStore(Store store, Integer pageNum, Integer pageSize);

    Response updateStore(Store store, HttpServletRequest request);

    Response deleteStore(List<Integer> idList);
}
