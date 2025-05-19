package com.example.springboottemplate.serviceimpl;

import com.example.springboottemplate.entity.EquUseLog;
import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.mapper.EquUseLogMapper;
import com.example.springboottemplate.service.EquUseLogService;
import com.example.springboottemplate.utils.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EquUseLogServiceimpl implements EquUseLogService {
    @Autowired
    private EquUseLogMapper equUseLogMapper;
    @Override
    public Response addEquUseLog(EquUseLog equUseLog) {
        equUseLogMapper.addEquUseLog(equUseLog);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findEquUseLog(EquUseLog equUseLog) {
        List<EquUseLog> list = equUseLogMapper.findEquUseLog(equUseLog);
        return new Response(200, list, "操作成功");
    }

    @Override
    public Response updateEquUseLog(EquUseLog equUseLog) {
        equUseLogMapper.updateEquUseLog(equUseLog);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteEquUseLog(List<Integer> idList) {
        if (ValidateUtil.isEmpty(idList)) {  // 使用工具类
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        int affectedRows = equUseLogMapper.deleteBatchIds(idList); // 调用mybatis-plus的逻辑删除，返回受影响行数
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        } else {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
    }
}
