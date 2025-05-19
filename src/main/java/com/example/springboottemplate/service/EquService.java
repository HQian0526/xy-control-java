package com.example.springboottemplate.service;

import com.example.springboottemplate.entity.Equ;
import com.example.springboottemplate.entity.Response;

import java.util.List;

public interface EquService {
    Response addEqu(Equ equ);

    Response findEqu(Equ equ);

    Response updateEqu(Equ equ);

    Response deleteEqu(List<Integer> idList);
}
