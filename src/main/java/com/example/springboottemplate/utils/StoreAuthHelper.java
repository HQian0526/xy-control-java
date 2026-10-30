package com.example.springboottemplate.utils;

import com.example.springboottemplate.dto.OwnedStoreVO;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.StoreStaff;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.mapper.StoreStaffMapper;
import com.example.springboottemplate.mapper.system.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class StoreAuthHelper {

    public static final String STORE_ID_HEADER = "X-Store-Id";

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private StoreStaffMapper storeStaffMapper;

    public Operator requireAdmin(HttpServletRequest request) {
        Operator operator = requireMerchantOrAdmin(request);
        if (!operator.isAdmin()) {
            throw new BusinessException("仅管理员可操作");
        }
        return operator;
    }

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
        return new Operator(userId, username, identityType, null, parseHeaderStoreId(request));
    }

    /** 商家、管理员，或该店启用中的员工（用于会员核验） */
    public Operator requireVerifyOperator(HttpServletRequest request) {
        User user = requireLoginUser(request);
        Integer identityType = user.getIdentityType();
        Long headerStoreId = parseHeaderStoreId(request);
        if (identityType != null && (identityType == 2 || identityType == 3)) {
            return new Operator(user.getId(), user.getUserName(), identityType, null, headerStoreId);
        }
        Long staffStoreId = findEnabledStaffStoreId(user.getId());
        if (staffStoreId != null) {
            return new Operator(user.getId(), user.getUserName(), identityType, staffStoreId, headerStoreId);
        }
        throw new BusinessException("无权操作");
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

    public Long parseHeaderStoreId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return parseStoreId(request.getHeader(STORE_ID_HEADER));
    }

    public Long parseStoreId(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            return Long.valueOf(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Long resolveWritableStoreId(Operator operator, Long requestStoreId) {
        if (operator.isMerchant()) {
            Long storeId = resolveCurrentOwnedStoreId(operator.getUserId(), firstNonNull(requestStoreId, operator.getHeaderStoreId()));
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

    public Long resolveVerifyStoreId(Operator operator, Long requestStoreId) {
        if (operator.isStaff()) {
            Long staffStoreId = operator.getStaffStoreId();
            Long requested = firstNonNull(requestStoreId, operator.getHeaderStoreId());
            if (requested != null && !staffStoreId.equals(requested)) {
                throw new BusinessException("无权操作其他店铺");
            }
            return staffStoreId;
        }
        return resolveWritableStoreId(operator, requestStoreId);
    }

    public void assertCanWriteStore(Operator operator, Long storeId) {
        if (operator.isAdmin()) {
            return;
        }
        if (storeId == null || !ownsStore(operator.getUserId(), storeId)) {
            throw new BusinessException("无权操作其他店铺");
        }
    }

    public Long applyListStoreScope(Operator operator, Long requestStoreId) {
        if (operator.isMerchant()) {
            return resolveCurrentOwnedStoreId(operator.getUserId(), firstNonNull(requestStoreId, operator.getHeaderStoreId()));
        }
        return requestStoreId;
    }

    public List<Store> listOwnedStores(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        Store query = new Store();
        query.setUserId(userId);
        query.setDeleted(0);
        List<Store> storeList = storeMapper.findStore(query);
        if (ValidateUtil.isEmpty(storeList)) {
            return Collections.emptyList();
        }
        List<Store> owned = new ArrayList<>();
        for (Store store : storeList) {
            if (store != null && store.getStoreId() != null) {
                owned.add(store);
            }
        }
        return owned;
    }

    public boolean ownsStore(Long userId, Long storeId) {
        if (userId == null || storeId == null) {
            return false;
        }
        return containsStore(listOwnedStores(userId), storeId);
    }

    public Long resolveCurrentOwnedStoreId(Long userId, Long requestedStoreId) {
        List<Store> owned = listOwnedStores(userId);
        if (owned.isEmpty()) {
            return null;
        }
        if (requestedStoreId != null) {
            if (!containsStore(owned, requestedStoreId)) {
                throw new BusinessException("无权操作其他店铺");
            }
            return requestedStoreId;
        }
        User user = userMapper.selectById(userId);
        Long lastStoreId = user == null ? null : user.getLastStoreId();
        if (lastStoreId != null && containsStore(owned, lastStoreId)) {
            return lastStoreId;
        }
        for (Store store : owned) {
            if (!StoreExpireHelper.isExpired(store)) {
                return store.getStoreId();
            }
        }
        return owned.get(0).getStoreId();
    }

    private boolean containsStore(List<Store> owned, Long storeId) {
        if (owned == null || storeId == null) {
            return false;
        }
        for (Store store : owned) {
            if (storeId.equals(store.getStoreId())) {
                return true;
            }
        }
        return false;
    }

    public String resolveMerchantBrowseStoreId(Long userId, String requestedStoreId) {
        List<Store> owned = listOwnedStores(userId);
        if (owned.isEmpty()) {
            return null;
        }
        if (StringUtils.hasText(requestedStoreId)) {
            return requestedStoreId.trim();
        }
        Long current = resolveCurrentOwnedStoreId(userId, null);
        return current == null ? null : String.valueOf(current);
    }

    /** 会员余额只在同一店长（商家账号）名下的门店共用。负责人不是店长时不归集。 */
    public Long findMerchantUserId(Long storeId) {
        if (storeId == null) {
            return null;
        }
        Store store = storeMapper.selectByStoreId(storeId);
        if (store == null || store.getUserId() == null) {
            return null;
        }
        User owner = userMapper.selectById(store.getUserId());
        if (owner == null || (owner.getDeleted() != null && owner.getDeleted() == 1)) {
            return null;
        }
        if (owner.getIdentityType() == null || owner.getIdentityType() != 2) {
            return null;
        }
        return owner.getId();
    }

    public List<OwnedStoreVO> toOwnedStoreVos(List<Store> stores) {
        List<OwnedStoreVO> list = new ArrayList<>();
        if (stores == null) {
            return list;
        }
        for (Store store : stores) {
            if (store == null || store.getStoreId() == null) {
                continue;
            }
            MiniappUiHelper.fillDefaults(store);
            list.add(OwnedStoreVO.builder()
                    .storeId(store.getStoreId())
                    .storeName(store.getStoreName())
                    .expired(StoreExpireHelper.isExpired(store))
                    .enablePay(MiniappUiHelper.enablePay(store) ? 1 : 0)
                    .shopStatus(store.getShopStatus())
                    .build());
        }
        return list;
    }

    public Long findEnabledStaffStoreId(Long userId) {
        if (userId == null) {
            return null;
        }
        StoreStaff staff = storeStaffMapper.selectEnabledByUserId(userId);
        return staff == null ? null : staff.getStoreId();
    }

    /** @deprecated 多门店后请用 resolveCurrentOwnedStoreId */
    public Long findMerchantStoreId(Long userId) {
        return resolveCurrentOwnedStoreId(userId, null);
    }

    public void fillStoreName(Long storeId, NameSetter setter) {
        if (storeId == null || setter == null) {
            return;
        }
        Store store = storeMapper.selectByStoreId(storeId);
        setter.setStoreName(store == null ? null : store.getStoreName());
    }

    public boolean isStoreExpired(Long storeId) {
        return StoreExpireHelper.isExpired(storeMapper, storeId);
    }

    private Long firstNonNull(Long first, Long second) {
        return first != null ? first : second;
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
        private final Long staffStoreId;
        private final Long headerStoreId;

        public Operator(Long userId, String username, Integer identityType) {
            this(userId, username, identityType, null, null);
        }

        public Operator(Long userId, String username, Integer identityType, Long staffStoreId) {
            this(userId, username, identityType, staffStoreId, null);
        }

        public Operator(Long userId, String username, Integer identityType, Long staffStoreId, Long headerStoreId) {
            this.userId = userId;
            this.username = username;
            this.identityType = identityType;
            this.staffStoreId = staffStoreId;
            this.headerStoreId = headerStoreId;
        }

        public boolean isAdmin() {
            return identityType != null && identityType == 3;
        }

        public boolean isMerchant() {
            return identityType != null && identityType == 2;
        }

        public boolean isStaff() {
            return staffStoreId != null && (identityType == null || identityType == 1);
        }
    }

    @FunctionalInterface
    public interface NameSetter {
        void setStoreName(String storeName);
    }
}
