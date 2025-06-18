package com.example.springboottemplate.serviceimpl;
import com.example.springboottemplate.entity.Area;
import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.mapper.AreaMapper;
import com.example.springboottemplate.service.AreaService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.springboottemplate.utils.ValidateUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Response findArea(Area area, Integer pageNum, Integer pageSize) {
        // 开启分页
        PageHelper.startPage(pageNum, pageSize);
        // 查询数据
        List<Area> list = areaMapper.findArea(area);
        // 封装分页结果
        PageInfo<Area> pageInfo = new PageInfo<>(list);
        // 构造返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getList());  // 当前页数据
        data.put("total", pageInfo.getTotal()); // 总记录数
        data.put("pages", pageInfo.getPages()); // 总页数
        data.put("pageNum", pageInfo.getPageNum()); // 当前页码
        data.put("pageSize", pageInfo.getPageSize()); // 每页数量
        return new Response(200, data, "操作成功");
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
