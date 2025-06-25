package com.example.springboottemplate.serviceimpl;

import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.entity.User;
import com.example.springboottemplate.mapper.UserMapper;
import com.example.springboottemplate.service.AuthService;
import com.example.springboottemplate.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AuthServiceimpl implements AuthService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;  // 注入 JwtUtil

    @Override
    public Response login(String username, String password) {
        User user = new User();
        user.setUserName(username);
        // 1. 查询用户
        List<User> list = userMapper.findUser(user);
        if (list.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        user = list.get(0); // 获取查询到的用户对象

        // 2. 验证密码 先注释后续用aes解密
//        if (!PasswordUtil.verifyPassword(password, user.getSalt(), user.getPassword())) {
//            throw new RuntimeException("用户名或密码错误");
//        }

        // 3. 验证账号状态
        if (user.getDeleted() != null && user.getDeleted() == 1) {
            throw new RuntimeException("账号已被冻结");
        }

        // 4. 生成token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUserName());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUserName());

        // 5. 返回token
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return new Response(200, tokens, "操作成功");
    }

    @Override
    public Response refreshToken(String refreshToken) {
        // 1. 验证refreshToken
        if (!jwtUtil.validateToken(refreshToken)) {
//            throw new RuntimeException("无效的refreshToken");
            return new Response(401, null, "无效的 refreshToken");
        }

        // 2. 解析token获取用户信息
        Claims claims = jwtUtil.parseToken(refreshToken);
        Integer userId = claims.get("userId", Integer.class);
        String username = claims.getSubject();

        // 3. 生成新的accessToken
        String newAccessToken = jwtUtil.generateAccessToken(userId, username);

        // 4. 返回新的accessToken
        Map<String, String> result = new HashMap<>();
        result.put("accessToken", newAccessToken);
        return new Response(200, result, "操作成功");
    }

    @Override
    public Response getAuthCode() {
        String[] str = new String[]{"AC_100100", "AC_100110", "AC_100120", "AC_100010"};
        return new Response(200, str, "操作成功");
    }
}
