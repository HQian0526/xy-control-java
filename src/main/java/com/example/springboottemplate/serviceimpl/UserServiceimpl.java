package com.example.springboottemplate.serviceimpl;
import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.entity.User;
import com.example.springboottemplate.mapper.UserMapper;
import com.example.springboottemplate.service.UserService;
import com.example.springboottemplate.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.springboottemplate.utils.ValidateUtil;
import java.util.List;

@Service
@Transactional
public class UserServiceimpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;  // 注入 JwtUtil
    @Override
    public Response addUser(User user) {
        userMapper.addUser(user);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findUser(User user) {
        List<User> list = userMapper.findUser(user);
        return new Response(200, list, "操作成功");
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
