package com.example.springboottemplate.serviceimpl;
import com.example.springboottemplate.entity.Area;
import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.mapper.AreaMapper;
import com.example.springboottemplate.service.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.springboottemplate.utils.ValidateUtil;
import java.util.List;

@Service
@Transactional
public class AreaServiceimpl implements AreaService {

    @Autowired
    private AreaMapper areaMapper;
    @Override
    public Response addArea(Area area) {
        areaMapper.addArea(area);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findArea(Area area) {
        List<Area> list = areaMapper.findArea(area);
        return new Response(200, list, "操作成功");
    }

    @Override
    public Response updateArea(Area area) {
        areaMapper.updateArea(area);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteArea(List<Integer> idList) {
        if (ValidateUtil.isEmpty(idList)) {  // 使用工具类
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        int affectedRows = areaMapper.deleteBatchIds(idList); // 调用mybatis-plus的逻辑删除，返回受影响行数
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        } else {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
    }
}
