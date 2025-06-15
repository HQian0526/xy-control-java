package com.example.springboottemplate.serviceimpl;

import com.example.springboottemplate.entity.Dict;
import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.mapper.DictMapper;
import com.example.springboottemplate.service.DictService;
import com.example.springboottemplate.utils.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DictServiceimpl implements DictService {
    @Autowired
    private DictMapper dictMapper;

    @Override
    public Response addDict(Dict dict) {
        dictMapper.addDict(dict);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findDict(Dict dict) {
        List<Dict> list = dictMapper.findDict(dict);
        return new Response(200, list, "操作成功");
    }

    @Override
    public Response updateDict(Dict dict) {
        dictMapper.updateDict(dict);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteDict(List<Integer> idList) {
        if (ValidateUtil.isEmpty(idList)) {  // 使用工具类
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        int affectedRows = dictMapper.deleteBatchIds(idList); // 调用mybatis-plus的逻辑删除，返回受影响行数
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        } else {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
    }
}
