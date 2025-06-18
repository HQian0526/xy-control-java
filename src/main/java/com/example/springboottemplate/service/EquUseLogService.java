package com.example.springboottemplate.service;

import com.example.springboottemplate.entity.EquUseLog;
import com.example.springboottemplate.entity.Response;

import java.util.List;

public interface EquUseLogService {
    Response addEquUseLog(EquUseLog equUseLog);

    Response findEquUseLog(EquUseLog equUseLog, Integer pageNum, Integer pageSize);

    Response updateEquUseLog(EquUseLog equUseLog);

    Response deleteEquUseLog(List<Integer> idList);
}
