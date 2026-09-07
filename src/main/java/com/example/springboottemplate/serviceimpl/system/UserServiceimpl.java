package com.example.springboottemplate.serviceimpl.system;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.mapper.StoreMapper;
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
import com.example.springboottemplate.config.UserProperties;
import com.example.springboottemplate.dto.ChangePasswordRequest;
import com.example.springboottemplate.utils.LogicDeleteHelper;
import com.example.springboottemplate.utils.PasswordUtil;
import com.example.springboottemplate.utils.ValidateUtil;
import org.springframework.util.StringUtils;

import java.util.Collections;
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
    private StoreMapper storeMapper;
    @Autowired
    private LogicDeleteHelper logicDeleteHelper;

    @Autowired
    private JwtUtil jwtUtil;  // 注入 JwtUtil

    @Autowired
    private UserProperties userProperties;

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
        boolean usedDefaultPassword = !StringUtils.hasText(user.getPassword());
        String plainPassword = usedDefaultPassword
                ? PasswordUtil.generateDefaultPassword(userProperties.getDefaultPasswordPrefix())
                : user.getPassword().trim();
        user.setPassword(PasswordUtil.encode(plainPassword));

        try {
            userMapper.addUser(user);
            if (usedDefaultPassword) {
                Map<String, Object> data = new HashMap<>();
                data.put("initialPassword", plainPassword);
                return Response.success(data);
            }
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
        for (User item : list) {
            if (item != null) {
                item.setPassword(null);
            }
        }
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
    public Response findCustomer(User user, Integer pageNum, Integer pageSize, HttpServletRequest request) {
        if (user == null) {
            user = new User();
        }
        Long operatorId = jwtUtil.tryGetUserId(request);
        User operator = operatorId == null ? null : userMapper.selectById(operatorId);
        Integer identityType = operator == null ? null : operator.getIdentityType();
        if (identityType == null || identityType == 1) {
            return buildUserPageResponse(Collections.emptyList(), pageNum, pageSize);
        }
        if (identityType == 2) {
            Long storeId = findMerchantStoreId(operatorId);
            if (storeId == null) {
                return buildUserPageResponse(Collections.emptyList(), pageNum, pageSize);
            }
            user.setBindStoreId(storeId);
        }
        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        List<User> list = userMapper.findCustomer(user);
        for (User item : list) {
            if (item != null) {
                item.setPassword(null);
            }
        }
        return buildUserPageResponse(list, pageNum, pageSize);
    }

    private Long findMerchantStoreId(Long userId) {
        if (userId == null) {
            return null;
        }
        Store query = new Store();
        query.setUserId(userId);
        query.setDeleted(0);
        List<Store> storeList = storeMapper.findStore(query);
        if (ValidateUtil.isEmpty(storeList) || storeList.get(0).getStoreId() == null) {
            return null;
        }
        return storeList.get(0).getStoreId();
    }

    private Response buildUserPageResponse(List<User> list, Integer pageNum, Integer pageSize) {
        PageInfo<User> pageInfo = new PageInfo<>(list);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getList());
        data.put("total", pageInfo.getTotal());
        data.put("pages", pageInfo.getPages());
        data.put("pageNum", pageNum != null ? pageInfo.getPageNum() : 1);
        data.put("pageSize", pageSize != null ? pageInfo.getPageSize() : pageInfo.getTotal());
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
        // 未传/空密码：不更新 password 列；有明文则转 BCrypt
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(null);
        } else if (!PasswordUtil.isBcrypt(user.getPassword())) {
            user.setPassword(PasswordUtil.encode(user.getPassword()));
        }
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
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Response.success(user);
    }

    @Override
    public Response resetPassword(Long id, HttpServletRequest request) {
        if (id == null) {
            return Response.fail(400, "用户ID不能为空");
        }
        User exist = userMapper.selectById(id);
        if (exist == null || (exist.getDeleted() != null && exist.getDeleted() == 1)) {
            return Response.fail(400, "用户不存在");
        }

        String token = request.getHeader("Authorization");
        String operator = null;
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            Claims claims = jwtUtil.parseToken(token.substring(7));
            operator = claims.getSubject();
        }

        String plainPassword = PasswordUtil.generateDefaultPassword(userProperties.getDefaultPasswordPrefix());
        User patch = new User();
        patch.setId(id);
        patch.setPassword(PasswordUtil.encode(plainPassword));
        patch.setUpdateBy(operator);
        userMapper.updateUser(patch);

        Map<String, Object> data = new HashMap<>();
        data.put("newPassword", plainPassword);
        data.put("id", String.valueOf(id));
        return Response.success(data);
    }

    @Override
    public Response changePassword(ChangePasswordRequest req, HttpServletRequest request) {
        if (req == null) {
            return Response.fail(400, "参数不能为空");
        }
        if (!StringUtils.hasText(req.getOldPassword())) {
            return Response.fail(400, "原密码不能为空");
        }
        if (!StringUtils.hasText(req.getNewPassword())) {
            return Response.fail(400, "新密码不能为空");
        }
        String newPassword = req.getNewPassword().trim();
        if (newPassword.length() <= 6) {
            return Response.fail(400, "新密码长度必须大于6位");
        }
        if (StringUtils.hasText(req.getConfirmPassword())
                && !newPassword.equals(req.getConfirmPassword().trim())) {
            return Response.fail(400, "两次输入的新密码不一致");
        }
        if (req.getOldPassword().trim().equals(newPassword)) {
            return Response.fail(400, "新密码不能与原密码相同");
        }

        Long userId = resolveCurrentUserId(request);
        if (userId == null) {
            return Response.fail(401, "无效的登录信息");
        }
        User user = userMapper.selectById(userId);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            return Response.fail(400, "用户不存在");
        }
        if (!PasswordUtil.matches(req.getOldPassword(), user.getPassword())) {
            return Response.fail(400, "原密码错误");
        }

        String operator = null;
        Object usernameAttr = request.getAttribute("username");
        if (usernameAttr instanceof String && StringUtils.hasText((String) usernameAttr)) {
            operator = (String) usernameAttr;
        } else {
            String token = request.getHeader("Authorization");
            if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
                operator = jwtUtil.parseToken(token.substring(7)).getSubject();
            }
        }

        User patch = new User();
        patch.setId(userId);
        patch.setPassword(PasswordUtil.encode(newPassword));
        patch.setUpdateBy(operator);
        userMapper.updateUser(patch);
        return Response.success();
    }

    private Long resolveCurrentUserId(HttpServletRequest request) {
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
        return userId;
    }
}
