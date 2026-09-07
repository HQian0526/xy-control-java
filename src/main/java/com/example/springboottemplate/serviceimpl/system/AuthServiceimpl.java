package com.example.springboottemplate.serviceimpl.system;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.WxSessionResult;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.mapper.system.UserMapper;
import com.example.springboottemplate.service.system.AuthService;
import com.example.springboottemplate.service.wx.WxApiService;
import com.example.springboottemplate.utils.JwtUtil;
import com.example.springboottemplate.utils.PasswordUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceimpl implements AuthService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private WxApiService wxApiService;

    @Override
    public Response login(String username, String password) {
        if (!StringUtils.hasText(username)) {
            throw new RuntimeException("用户名不能为空");
        }
        if (!StringUtils.hasText(password)) {
            throw new RuntimeException("密码不能为空");
        }
        // 1. 精确查询用户（避免 findUser 的 LIKE 误匹配）
        User user = userMapper.selectByUserName(username.trim());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 2. 验证密码（BCrypt；兼容历史明文，成功后升级入库）
        if (!PasswordUtil.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (PasswordUtil.needsRehash(user.getPassword())) {
            User patch = new User();
            patch.setId(user.getId());
            patch.setPassword(PasswordUtil.encode(password));
            userMapper.updateUser(patch);
            user.setPassword(patch.getPassword());
        }

        // 3. 验证账号状态
        if (user.getDeleted() != null && user.getDeleted() == 1) {
            throw new RuntimeException("账号已被冻结");
        }

        // 4. 生成token
        return buildLoginResponse(user, false);
    }

    @Override
    public Response wxLogin(String code) {
        if (!StringUtils.hasText(code)) {
            throw new RuntimeException("微信登录 code 不能为空");
        }

        WxSessionResult session = wxApiService.code2Session(code.trim());
        if (session.getErrcode() != null && session.getErrcode() != 0) {
            throw new RuntimeException("微信登录失败: " + session.getErrmsg() + "(" + session.getErrcode() + ")");
        }
        if (!StringUtils.hasText(session.getOpenid())) {
            throw new RuntimeException("微信登录失败: 未获取到 openid");
        }

        User user = userMapper.selectByOpenid(session.getOpenid());
        if (user == null) {
            user = createWxUser(session);
        } else if (StringUtils.hasText(session.getUnionid())
                && !session.getUnionid().equals(user.getUnionid())) {
            user.setUnionid(session.getUnionid());
            userMapper.updateUser(user);
        }

        if (user.getDeleted() != null && user.getDeleted() == 1) {
            throw new RuntimeException("账号已被冻结");
        }

        return buildLoginResponse(user, false);
    }

    @Override
    public Response bindPhone(String phoneCode, HttpServletRequest request) {
        Long currentUserId = resolveCurrentUserId(request);
        if (currentUserId == null) {
            throw new RuntimeException("未登录或登录已失效");
        }
        if (!StringUtils.hasText(phoneCode)) {
            throw new RuntimeException("手机号授权 code 不能为空");
        }

        User current = userMapper.selectById(currentUserId);
        if (current == null || (current.getDeleted() != null && current.getDeleted() == 1)) {
            throw new RuntimeException("当前用户不存在或已冻结");
        }
        if (!StringUtils.hasText(current.getOpenid())) {
            throw new RuntimeException("当前账号未绑定微信，无法通过微信授权手机号");
        }

        String phone = normalizePhone(wxApiService.getPhoneNumber(phoneCode.trim()));
        if (!StringUtils.hasText(phone)) {
            throw new RuntimeException("未能获取到有效手机号");
        }

        // 当前账号已是该手机号，直接返回
        if (phone.equals(current.getPhone())) {
            return buildLoginResponse(current, false);
        }

        User phoneOwner = userMapper.selectByPhone(phone);

        // 手机号未被占用：只给当前微信用户补手机号
        if (phoneOwner == null) {
            current.setPhone(phone);
            userMapper.updateUser(current);
            return buildLoginResponse(current, false);
        }

        // 查到的就是自己（数据异常兜底）
        if (Objects.equals(phoneOwner.getId(), current.getId())) {
            return buildLoginResponse(current, false);
        }

        // 该手机号已绑定其他微信
        if (StringUtils.hasText(phoneOwner.getOpenid())
                && !phoneOwner.getOpenid().equals(current.getOpenid())) {
            throw new RuntimeException("该手机号已绑定其他微信账号，请联系管理员处理");
        }

        // 合并：以手机号对应的后台账号为主账号，挂上当前 openid，清理临时 wx 用户
        return mergeWxUserIntoPhoneOwner(current, phoneOwner);
    }

    /**
     * 优先读拦截器/过滤器写入的 userId；没有则从 Authorization 头解析（兼容 context-path 导致拦截器未命中）
     */
    private Long resolveCurrentUserId(HttpServletRequest request) {
        Object attr = request.getAttribute("userId");
        if (attr instanceof Long) {
            return (Long) attr;
        }
        if (attr instanceof Number) {
            return ((Number) attr).longValue();
        }
        if (attr instanceof String && StringUtils.hasText((String) attr)) {
            try {
                return Long.valueOf((String) attr);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }

        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return null;
        }
        return jwtUtil.getUserId(jwtUtil.parseToken(token));
    }

    /**
     * 将临时微信用户合并进已有手机号账号
     */
    private Response mergeWxUserIntoPhoneOwner(User wxUser, User phoneOwner) {
        String openid = wxUser.getOpenid();
        String unionid = StringUtils.hasText(wxUser.getUnionid()) ? wxUser.getUnionid() : phoneOwner.getUnionid();

        // 1. 先从临时用户摘掉 openid，避免 uk_user_openid 冲突
        userMapper.detachWxAndSoftDelete(wxUser.getId());

        // 2. 主账号挂上微信身份
        phoneOwner.setOpenid(openid);
        if (StringUtils.hasText(unionid)) {
            phoneOwner.setUnionid(unionid);
        }
        userMapper.updateUser(phoneOwner);

        User merged = userMapper.selectById(phoneOwner.getId());
        if (merged == null) {
            throw new RuntimeException("账号合并失败，请重试");
        }
        return buildLoginResponse(merged, true);
    }

    private String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return phone;
        }
        String value = phone.trim().replace(" ", "");
        if (value.startsWith("+86")) {
            value = value.substring(3);
        } else if (value.startsWith("86") && value.length() == 13) {
            value = value.substring(2);
        }
        return value;
    }

    private User createWxUser(WxSessionResult session) {
        String openid = session.getOpenid();
        // user_name 字段较短，不能直接存完整 openid；用 md5 截断保证唯一且长度可控
        String userName = "wx_" + DigestUtils.md5DigestAsHex(openid.getBytes(StandardCharsets.UTF_8))
                .substring(0, 12);

        // 合并账号后临时用户会逻辑删除并清空 openid，但 user_name 仍占用唯一索引。
        // 再次登录时按 openid 查不到，需要复用/恢复同名账号，避免 Duplicate entry user_name。
        User existing = userMapper.selectByUserNameAny(userName);
        if (existing != null) {
            return reuseOrRestoreWxUser(existing, session);
        }

        User user = User.builder()
                .id(IdWorker.getId())
                .userName(userName)
                .realName(defaultWxRealName())
                .identityType(1)
                .openid(openid)
                .unionid(session.getUnionid())
                .createdTime(new Date())
                .createdBy("wx")
                .deleted(0)
                .build();
        try {
            userMapper.addUser(user);
        } catch (DuplicateKeyException e) {
            // 并发或历史脏数据兜底：再按用户名取一次
            User conflict = userMapper.selectByUserNameAny(userName);
            if (conflict != null) {
                return reuseOrRestoreWxUser(conflict, session);
            }
            // user_name 仍冲突则换一个后缀再插一次
            user.setId(IdWorker.getId());
            user.setUserName(userName + "_" + UUID.randomUUID().toString().substring(0, 4));
            userMapper.addUser(user);
        }
        if (user.getId() == null) {
            User saved = userMapper.selectByOpenid(openid);
            if (saved == null) {
                throw new RuntimeException("创建微信用户失败");
            }
            return saved;
        }
        return user;
    }

    /**
     * 复用已存在的 wx_ 用户名记录：恢复逻辑删除、重新绑定 openid
     */
    private User reuseOrRestoreWxUser(User existing, WxSessionResult session) {
        String openid = session.getOpenid();
        // 已是正常账号且 openid 属于别人：不应抢绑，换新用户名新建
        if (existing.getDeleted() != null && existing.getDeleted() == 0
                && StringUtils.hasText(existing.getOpenid())
                && !openid.equals(existing.getOpenid())) {
            String userName = existing.getUserName() + "_" + UUID.randomUUID().toString().substring(0, 4);
            User user = User.builder()
                    .id(IdWorker.getId())
                    .userName(userName)
                    .realName(defaultWxRealName())
                    .identityType(1)
                    .openid(openid)
                    .unionid(session.getUnionid())
                    .createdTime(new Date())
                    .createdBy("wx")
                    .deleted(0)
                    .build();
            userMapper.addUser(user);
            if (user.getId() == null) {
                User saved = userMapper.selectByOpenid(openid);
                if (saved == null) {
                    throw new RuntimeException("创建微信用户失败");
                }
                return saved;
            }
            return user;
        }

        existing.setOpenid(openid);
        if (StringUtils.hasText(session.getUnionid())) {
            existing.setUnionid(session.getUnionid());
        }
        existing.setDeleted(0);
        if (!StringUtils.hasText(existing.getRealName())) {
            existing.setRealName(defaultWxRealName());
        }
        if (existing.getIdentityType() == null) {
            existing.setIdentityType(1);
        }
        userMapper.updateUser(existing);
        User refreshed = userMapper.selectById(existing.getId());
        return refreshed != null ? refreshed : existing;
    }

    /** 无微信昵称时：微信用户 + 当前时间戳 MD5 后 5 位 */
    private String defaultWxRealName() {
        String hash = DigestUtils.md5DigestAsHex(
                String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
        return "微信用户" + hash.substring(hash.length() - 5);
    }

    private Response buildLoginResponse(User user, boolean merged) {
        if (user == null || user.getId() == null) {
            throw new RuntimeException("登录失败：用户ID为空");
        }
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUserName());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUserName());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("userName", user.getUserName());
        userInfo.put("realName", user.getRealName());
        userInfo.put("phone", user.getPhone());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("identityType", user.getIdentityType());
        userInfo.put("bindStoreId", user.getBindStoreId());
        userInfo.put("openid", user.getOpenid());
        userInfo.put("needBindPhone", !StringUtils.hasText(user.getPhone()));

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("refreshToken", refreshToken);
        // 兼容小程序 auth.js：优先读 token / userInfo
        data.put("token", accessToken);
        data.put("userInfo", userInfo);
        data.put("merged", merged);
        return new Response(200, data, merged ? "账号已合并" : "操作成功");
    }

    @Override
    public Response refreshToken(String refreshToken) {
        // 1. 验证refreshToken
        if (!jwtUtil.validateToken(refreshToken)) {
            return new Response(401, null, "无效的 refreshToken");
        }

        // 2. 解析token获取用户信息
        Claims claims = jwtUtil.parseToken(refreshToken);
        Long userId = jwtUtil.getUserId(claims);
        String username = claims.getSubject();
        if (userId == null) {
            return new Response(401, null, "无效的 refreshToken");
        }

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
