package com.example.springboottemplate.service.system;

import com.example.springboottemplate.entity.system.Dict;
import com.example.springboottemplate.dto.Response;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface DictService {
    Response addDict(Dict dict, HttpServletRequest request);

    Response findDict(Dict dict, Integer pageNum, Integer pageSize);

    Response updateDict(Dict dict);

    Response deleteDict(List<Integer> idList);
}
