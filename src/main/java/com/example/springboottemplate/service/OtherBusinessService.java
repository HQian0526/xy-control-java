package com.example.springboottemplate.service;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.OtherBusiness;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface OtherBusinessService {
    Response addOtherBusiness(OtherBusiness otherBusiness, HttpServletRequest request);

    Response findOtherBusiness(OtherBusiness otherBusiness, Integer pageNum, Integer pageSize, HttpServletRequest request);

    Response updateOtherBusiness(OtherBusiness otherBusiness, HttpServletRequest request);

    Response deleteOtherBusiness(List<Long> idList);
}
