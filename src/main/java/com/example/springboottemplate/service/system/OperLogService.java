package com.example.springboottemplate.service.system;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.system.OperLog;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface OperLogService {
    Response addOperLog(OperLog operLog, HttpServletRequest request);

    Response findOperLog(OperLog operLog, Integer pageNum, Integer pageSize);

    Response updateOperLog(OperLog operLog, HttpServletRequest request);

    Response deleteOperLog(List<Long> idList);
}
