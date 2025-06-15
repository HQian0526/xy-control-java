package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.Dict;

import java.util.List;

public interface DictMapper extends BaseMapper<Dict> {
    void addDict(Dict dict);  //新增字典

    List<Dict> findDict(Dict dict); //查找所有字典

    void updateDict(Dict dict); //修改字典信息
}
