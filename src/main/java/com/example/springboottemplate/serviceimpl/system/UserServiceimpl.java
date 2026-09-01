package com.example.springboottemplate.serviceimpl.system;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
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
import com.example.springboottemplate.utils.LogicDeleteHelper;
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
    private LogicDeleteHelper logicDeleteHelper;

    @Autowired
    private JwtUtil jwtUtil;  // 注入 JwtUtil

    @Override
    public Response addUser(User user, HttpServletRequest request) {
        // 1. 从请求头中获取JWT令牌
        String token = request.getHeader("Authorization").substring(7);
        // 2. 解析令牌获取用户名
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        // 3. 系统生成雪花 id，忽略前端传入
        user.setId(IdWorker.getId());
        user.setCreatedTime(new Date());
        user.setCreatedBy(username);
        if (user.getDeleted() == null) {
            user.setDeleted(0);
        }

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
        // 传了分页参数才开启分页，否则返回全部数据
        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
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
    public Response updateUser(User user, HttpServletRequest request) {
        // 1. 从请求头中获取JWT令牌
        String token = request.getHeader("Authorization").substring(7);
        // 2. 解析令牌获取用户名
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        user.setUpdateBy(username);
        userMapper.updateUser(user);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteUser(List<Long> idList) {
        if (ValidateUtil.isEmpty(idList)) {  // 使用工具类
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        int affectedRows = logicDeleteHelper.deleteByIds("user", idList);
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        } else {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
    }

    @Override
    public Response getUserInfo(HttpServletRequest request) {
        // 优先用拦截器已解析的 userId；否则再从 Authorization 解析
        Long userId = null;
        Object attr = request.getAttribute("userId");
        if (attr instanceof Long) {
            userId = (Long) attr;
        } else if (attr instanceof Number) {
            userId = ((Number) attr).longValue();
        } else if (attr instanceof String && !((String) attr).isBlank()) {
            try {
                userId = Long.valueOf(((String) attr).trim());
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        if (userId == null) {
            userId = jwtUtil.tryGetUserId(request);
        }
        if (userId == null) {
            return new Response(401, null, "无效的登录信息");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return new Response(200, null, "操作成功");
        }
        user.setPassword(null); // 不返回密码
        return new Response(200, user, "操作成功");
    }

    @Override
    public Response getUserById(Long id) {
        return Response.success(userMapper.selectById(id));
    }
}
