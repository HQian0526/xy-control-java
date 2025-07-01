package com.example.springboottemplate.service;

import com.example.springboottemplate.entity.Area;
import com.example.springboottemplate.dto.Response;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface AreaService {
    Response addArea(Area area, HttpServletRequest request);

    Response findArea(Area area, Integer pageNum, Integer pageSize);

    Response updateArea(Area area);

    Response deleteArea(List<Integer> idList);
}
