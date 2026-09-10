package com.example.springboottemplate.utils;

import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.mapper.system.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class StoreAuthHelper {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StoreMapper storeMapper;

    public Operator requireMerchantOrAdmin(HttpServletRequest request) {
        Claims claims = parseClaims(request);
        Long userId = jwtUtil.getUserId(claims);
        String username = claims.getSubject();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        User user = userMapper.selectById(userId);
        Integer identityType = user == null ? null : user.getIdentityType();
        if (identityType == null || identityType == 1) {
            throw new BusinessException("无权操作");
        }
        return new Operator(userId, username, identityType);
    }

    public User requireLoginUser(HttpServletRequest request) {
        Claims claims = parseClaims(request);
        Long userId = jwtUtil.getUserId(claims);
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    public Long tryGetUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        try {
            String auth = request.getHeader("Authorization");
            if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
                return null;
            }
            String token = auth.substring(7);
            try {
                if (!jwtUtil.validateToken(token)) {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
            return jwtUtil.getUserId(jwtUtil.parseToken(token));
        } catch (Exception e) {
            return null;
        }
    }

    public Long resolveWritableStoreId(Operator operator, Long requestStoreId) {
        if (operator.isMerchant()) {
            Long storeId = findMerchantStoreId(operator.getUserId());
            if (storeId == null) {
                throw new BusinessException("当前账号未绑定店铺");
            }
            return storeId;
        }
        if (requestStoreId == null) {
            throw new BusinessException("请选择店铺");
        }
        Store store = storeMapper.selectByStoreId(requestStoreId);
        if (store == null) {
            throw new BusinessException("店铺不存在");
        }
        return requestStoreId;
    }

    public void assertCanWriteStore(Operator operator, Long storeId) {
        if (operator.isAdmin()) {
            return;
        }
        Long mine = findMerchantStoreId(operator.getUserId());
        if (mine == null || storeId == null || !mine.equals(storeId)) {
            throw new BusinessException("无权操作其他店铺");
        }
    }

    public Long applyListStoreScope(Operator operator, Long requestStoreId) {
        if (operator.isMerchant()) {
            return findMerchantStoreId(operator.getUserId());
        }
        return requestStoreId;
    }

    public Long findMerchantStoreId(Long userId) {
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

    public void fillStoreName(Long storeId, NameSetter setter) {
        if (storeId == null || setter == null) {
            return;
        }
        Store store = storeMapper.selectByStoreId(storeId);
        setter.setStoreName(store == null ? null : store.getStoreName());
    }

    private Claims parseClaims(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
            throw new BusinessException("未登录");
        }
        return jwtUtil.parseToken(auth.substring(7));
    }

    @Getter
    public static class Operator {
        private final Long userId;
        private final String username;
        private final Integer identityType;

        public Operator(Long userId, String username, Integer identityType) {
            this.userId = userId;
            this.username = username;
            this.identityType = identityType;
        }

        public boolean isAdmin() {
            return identityType != null && identityType == 3;
        }

        public boolean isMerchant() {
            return identityType != null && identityType == 2;
        }
    }

    @FunctionalInterface
    public interface NameSetter {
        void setStoreName(String storeName);
    }
}
