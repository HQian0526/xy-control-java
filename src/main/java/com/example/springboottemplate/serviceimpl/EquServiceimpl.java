package com.example.springboottemplate.serviceimpl;
import com.example.springboottemplate.entity.Equ;
import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.mapper.EquMapper;
import com.example.springboottemplate.service.EquService;
import com.example.springboottemplate.utils.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class EquServiceimpl implements EquService {
    @Autowired
    private EquMapper equMapper;
    @Override
    public Response addEqu(Equ equ) {
        equMapper.addEqu(equ);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findEqu(Equ equ) {
        List<Equ> list = equMapper.findEqu(equ);
        return new Response(200, list, "操作成功");
    }

    @Override
    public Response updateEqu(Equ equ) {
        equMapper.updateEqu(equ);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteEqu(List<Integer> idList) {
        if (ValidateUtil.isEmpty(idList)) {  // 使用工具类
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        int affectedRows = equMapper.deleteBatchIds(idList); // 调用mybatis-plus的逻辑删除，返回受影响行数
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        } else {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
    }
}
