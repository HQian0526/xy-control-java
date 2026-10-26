package com.example.springboottemplate.service;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.StoreMiniappUiSaveRequest;
import com.example.springboottemplate.dto.StoreWxConfigSaveRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface StoreWxConfigService {
    Response getByStoreId(Long storeId, HttpServletRequest request);

    Response list(HttpServletRequest request);

    Response save(StoreWxConfigSaveRequest body, HttpServletRequest request);

    Response saveMiniappUi(StoreMiniappUiSaveRequest body, HttpServletRequest request);
}
