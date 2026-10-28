package com.example.springboottemplate.service;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.StoreStaff;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface StoreStaffService {
    Response addStoreStaff(StoreStaff record, HttpServletRequest request);

    Response findStoreStaff(StoreStaff record, Integer pageNum, Integer pageSize, HttpServletRequest request);

    Response updateStoreStaff(StoreStaff record, HttpServletRequest request);

    Response deleteStoreStaff(List<Long> idList, HttpServletRequest request);
}
