package com.example.springboottemplate.service;

import com.example.springboottemplate.entity.Area;
import com.example.springboottemplate.entity.Response;

import java.util.List;

public interface AreaService {
    Response addArea(Area area);

    Response findArea(Area area);

    Response updateArea(Area area);

    Response deleteArea(List<Integer> idList);
}
