package com.example.springboottemplate.service;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.StoreVerifyLog;
import jakarta.servlet.http.HttpServletRequest;

public interface StoreVerifyLogService {
    Response findStoreVerifyLog(StoreVerifyLog record, Integer pageNum, Integer pageSize, HttpServletRequest request);
}
