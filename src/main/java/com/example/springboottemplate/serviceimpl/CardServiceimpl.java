package com.example.springboottemplate.serviceimpl;

import com.example.springboottemplate.entity.Card;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.mapper.CardMapper;
import com.example.springboottemplate.service.CardService;
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
public class CardServiceimpl implements CardService {
    @Autowired
    private CardMapper cardMapper;
    @Autowired
    private JwtUtil jwtUtil;  // 注入 JwtUtil

    @Override
    public Response addCard(Card card, HttpServletRequest request) {
        // 1. 从请求头中获取JWT令牌
        String token = request.getHeader("Authorization").substring(7);
        // 2. 解析令牌获取用户名
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        card.setCreatedTime(new Date());
        card.setCreatedBy(username);

        cardMapper.addCard(card);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findCard(Card card, Integer pageNum, Integer pageSize) {
        // 开启分页
        PageHelper.startPage(pageNum, pageSize);
        // 查询数据
        List<Card> list = cardMapper.findCard(card);
        // 封装分页结果
        PageInfo<Card> pageInfo = new PageInfo<>(list);
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
    public Response updateCard(Card card) {
        cardMapper.updateCard(card);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteCard(List<Integer> idList) {
        if (ValidateUtil.isEmpty(idList)) {  // 使用工具类
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        int affectedRows = cardMapper.deleteBatchIds(idList); // 调用mybatis-plus的逻辑删除，返回受影响行数
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        } else {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
    }
}
