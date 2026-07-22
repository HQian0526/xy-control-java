package com.example.springboottemplate.service;

import com.example.springboottemplate.entity.Catagory;
import com.example.springboottemplate.dto.Response;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface CatagoryService {
    Response addCatagory(Catagory catagory, HttpServletRequest request);

    Response findCatagory(Catagory catagory, Integer pageNum, Integer pageSize, HttpServletRequest request);

    Response updateCatagory(Catagory catagory, HttpServletRequest request);

    Response deleteCatagory(List<Integer> idList);
}
