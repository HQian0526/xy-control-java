package com.example.springboottemplate.service;

import com.example.springboottemplate.entity.Equ;
import com.example.springboottemplate.dto.Response;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface EquService {
    Response addEqu(Equ equ, HttpServletRequest request);

    Response findEqu(Equ equ, Integer pageNum, Integer pageSize);

    Response updateEqu(Equ equ, HttpServletRequest request);

    Response deleteEqu(List<Integer> idList);
}
