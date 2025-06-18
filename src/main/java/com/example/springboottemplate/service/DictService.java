package com.example.springboottemplate.service;

import com.example.springboottemplate.entity.Dict;
import com.example.springboottemplate.entity.Response;

import java.util.List;

public interface DictService {
    Response addDict(Dict dict);

    Response findDict(Dict dict, Integer pageNum, Integer pageSize);

    Response updateDict(Dict dict);

    Response deleteDict(List<Integer> idList);
}
