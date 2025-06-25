package com.example.springboottemplate.serviceimpl;
import com.example.springboottemplate.entity.Equ;
import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.mapper.EquMapper;
import com.example.springboottemplate.service.EquService;
import com.example.springboottemplate.utils.JwtUtil;
import com.example.springboottemplate.utils.ValidateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EquServiceimpl implements EquService {
    @Autowired
    private EquMapper equMapper;
    @Autowired
    private JwtUtil jwtUtil;  // 注入 JwtUtil

    @Override
    public Response addEqu(Equ equ, HttpServletRequest request) {
        // 1. 从请求头中获取JWT令牌
        String token = request.getHeader("Authorization").substring(7);
        // 2. 解析令牌获取用户名
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        equ.setCreatedTime(new Date());
        equ.setCreatedBy(username);

        equMapper.addEqu(equ);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findEqu(Equ equ, Integer pageNum, Integer pageSize) {
        // 开启分页
        PageHelper.startPage(pageNum, pageSize);
        // 查询数据
        List<Equ> list = equMapper.findEqu(equ);
        // 封装分页结果
        PageInfo<Equ> pageInfo = new PageInfo<>(list);
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
