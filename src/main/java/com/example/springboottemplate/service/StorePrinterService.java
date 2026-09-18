package com.example.springboottemplate.service;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.StorePrinterSaveRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface StorePrinterService {
    Response getByStoreId(Long storeId, HttpServletRequest request);

    Response list(HttpServletRequest request);

    Response save(StorePrinterSaveRequest body, HttpServletRequest request);

    Response queryStatus(Long storeId, HttpServletRequest request);

    Response testPrint(Long storeId, HttpServletRequest request);

    Response reprint(String orderNo, HttpServletRequest request);
}
