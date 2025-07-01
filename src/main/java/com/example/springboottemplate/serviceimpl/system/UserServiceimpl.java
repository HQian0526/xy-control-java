package com.example.springboottemplate.serviceimpl.system;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.mapper.system.UserMapper;
import com.example.springboottemplate.service.system.UserService;
import com.example.springboottemplate.utils.JwtUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.springboottemplate.utils.ValidateUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class UserServiceimpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;  // 注入 JwtUtil

    @Override
    public Response addUser(User user, HttpServletRequest request) {
        // 1. 从请求头中获取JWT令牌
        String token = request.getHeader("Authorization").substring(7);
        // 2. 解析令牌获取用户名
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        user.setCreatedTime(new Date());
        user.setCreatedBy(username);

        try {
            userMapper.addUser(user);
            return Response.success();
        } catch (DuplicateKeyException e) {
            // 解析错误信息（不同数据库错误信息格式不同）
            if (e.getMessage().contains("user_name")) {
                return Response.fail("用户名已存在");
            }
            return Response.fail("操作失败，请联系管理员");
        }

    }

    @Override
    public Response findUser(User user, Integer pageNum, Integer pageSize) {
        // 开启分页
        PageHelper.startPage(pageNum, pageSize);
        // 查询数据
        List<User> list = userMapper.findUser(user);
        // 封装分页结果
        PageInfo<User> pageInfo = new PageInfo<>(list);
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
    public Response updateUser(User user) {
        userMapper.updateUser(user);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteUser(List<Integer> idList) {
        if (ValidateUtil.isEmpty(idList)) {  // 使用工具类
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        int affectedRows = userMapper.deleteBatchIds(idList); // 调用mybatis-plus的逻辑删除，返回受影响行数
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        } else {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
    }

    @Override
    public Response getUserInfo(HttpServletRequest request) {
        // 1. 从请求头中获取JWT令牌
        String token = request.getHeader("Authorization").substring(7);
        // 2. 解析令牌获取用户名
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();

        // 3. 查询数据库获取用户详细信息
        User user = new User();
        user.setUserName(username);
        List<User> list = userMapper.findUser(user);
        if (list.isEmpty()) {
            return new Response(200, null, "操作成功");
        } else {
            return new Response(200, list.get(0), "操作成功");
        }
    }
}
