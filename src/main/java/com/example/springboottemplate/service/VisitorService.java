package com.example.springboottemplate.service;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.Visitor;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface VisitorService {
    Response addVisitor(Visitor visitor, HttpServletRequest request);

    Response findVisitor(Visitor visitor, Integer pageNum, Integer pageSize, HttpServletRequest request);

    Response updateVisitor(Visitor visitor, HttpServletRequest request);

    Response deleteVisitor(List<Long> idList);
}
