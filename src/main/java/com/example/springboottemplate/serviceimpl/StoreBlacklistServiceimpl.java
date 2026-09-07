package com.example.springboottemplate.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.StoreBlacklist;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.StoreBlacklistMapper;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.mapper.system.UserMapper;
import com.example.springboottemplate.service.StoreBlacklistService;
import com.example.springboottemplate.utils.JwtUtil;
import com.example.springboottemplate.utils.LogicDeleteHelper;
import com.example.springboottemplate.utils.ValidateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StoreBlacklistServiceimpl implements StoreBlacklistService {

    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";

    @Autowired
    private StoreBlacklistMapper storeBlacklistMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private LogicDeleteHelper logicDeleteHelper;

    @Override
    public Response addStoreBlacklist(StoreBlacklist record, HttpServletRequest request) {
        if (record == null) {
            throw new BusinessException("参数不能为空");
        }
        Claims claims = parseClaims(request);
        Long operatorId = jwtUtil.getUserId(claims);
        String operator = claims.getSubject();
        User operatorUser = operatorId == null ? null : userMapper.selectById(operatorId);
        Integer identityType = operatorUser == null ? null : operatorUser.getIdentityType();
        if (identityType == null || identityType == 1) {
            throw new BusinessException("无权操作黑名单");
        }

        Long storeId = resolveWritableStoreId(identityType, operatorId, record.getStoreId());
        record.setStoreId(storeId);

        fillTarget(record);

        StoreBlacklist exist = findExisting(storeId, record.getUserId(), record.getPhone());
        if (exist != null && (exist.getDeleted() == null || exist.getDeleted() == 0)) {
            throw new BusinessException("该用户或手机号已在本店黑名单中");
        }

        if (exist != null) {
            StoreBlacklist restore = new StoreBlacklist();
            restore.setId(exist.getId());
            restore.setUserId(record.getUserId());
            restore.setPhone(record.getPhone());
            restore.setRealName(record.getRealName());
            restore.setReason(record.getReason());
            restore.setRemark(record.getRemark());
            restore.setUpdateBy(operator);
            restore.setDeleted(0);
            storeBlacklistMapper.updateStoreBlacklist(restore);
            return Response.success();
        }

        record.setId(IdWorker.getId());
        record.setDeleted(0);
        record.setCreatedTime(new Date());
        record.setCreatedBy(operator);
        try {
            storeBlacklistMapper.addStoreBlacklist(record);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("该用户或手机号已在本店黑名单中");
        }
        return Response.success();
    }

    @Override
    public Response findStoreBlacklist(StoreBlacklist record, Integer pageNum, Integer pageSize,
                                       HttpServletRequest request) {
        if (record == null) {
            record = new StoreBlacklist();
        }
        Claims claims = parseClaims(request);
        Long operatorId = jwtUtil.getUserId(claims);
        User operatorUser = operatorId == null ? null : userMapper.selectById(operatorId);
        Integer identityType = operatorUser == null ? null : operatorUser.getIdentityType();
        if (identityType == null || identityType == 1) {
            return buildPageResponse(Collections.emptyList(), pageNum, pageSize);
        }
        if (identityType == 2) {
            Long storeId = findMerchantStoreId(operatorId);
            if (storeId == null) {
                return buildPageResponse(Collections.emptyList(), pageNum, pageSize);
            }
            record.setStoreId(storeId);
        }

        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        List<StoreBlacklist> list = storeBlacklistMapper.findStoreBlacklist(record);
        list.forEach(item -> {
            if (item.getStoreId() != null) {
                Store store = storeMapper.selectByStoreId(item.getStoreId());
                item.setStoreName(store != null ? store.getStoreName() : null);
            }
        });
        return buildPageResponse(list, pageNum, pageSize);
    }

    @Override
    public Response updateStoreBlacklist(StoreBlacklist record, HttpServletRequest request) {
        if (record == null || record.getId() == null) {
            throw new BusinessException("记录ID不能为空");
        }
        Claims claims = parseClaims(request);
        Long operatorId = jwtUtil.getUserId(claims);
        String operator = claims.getSubject();
        User operatorUser = operatorId == null ? null : userMapper.selectById(operatorId);
        Integer identityType = operatorUser == null ? null : operatorUser.getIdentityType();
        if (identityType == null || identityType == 1) {
            throw new BusinessException("无权操作黑名单");
        }

        StoreBlacklist exist = storeBlacklistMapper.selectById(record.getId());
        if (exist == null || (exist.getDeleted() != null && exist.getDeleted() == 1)) {
            throw new BusinessException("记录不存在");
        }
        assertCanWriteStore(identityType, operatorId, exist.getStoreId());

        StoreBlacklist patch = new StoreBlacklist();
        patch.setId(record.getId());
        patch.setRealName(record.getRealName());
        patch.setReason(record.getReason());
        patch.setRemark(record.getRemark());
        patch.setUpdateBy(operator);
        storeBlacklistMapper.updateStoreBlacklist(patch);
        return Response.success();
    }

    @Override
    public Response deleteStoreBlacklist(List<Long> idList, HttpServletRequest request) {
        if (ValidateUtil.isEmpty(idList)) {
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        Claims claims = parseClaims(request);
        Long operatorId = jwtUtil.getUserId(claims);
        User operatorUser = operatorId == null ? null : userMapper.selectById(operatorId);
        Integer identityType = operatorUser == null ? null : operatorUser.getIdentityType();
        if (identityType == null || identityType == 1) {
            throw new BusinessException("无权操作黑名单");
        }

        List<Long> allowed = new ArrayList<>();
        for (Long id : idList) {
            if (id == null) {
                continue;
            }
            StoreBlacklist exist = storeBlacklistMapper.selectById(id);
            if (exist == null || (exist.getDeleted() != null && exist.getDeleted() == 1)) {
                continue;
            }
            try {
                assertCanWriteStore(identityType, operatorId, exist.getStoreId());
                allowed.add(id);
            } catch (BusinessException ignored) {
                // 跳过无权删除的记录
            }
        }
        if (allowed.isEmpty()) {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
        int affectedRows = logicDeleteHelper.deleteByIds("store_blacklist", allowed);
        if (affectedRows > 0) {
            return Response.success();
        }
        return new Response(400, null, "操作失败，未找到需要删除的记录");
    }

    @Override
    public boolean isBlacklisted(Long storeId, Long userId, String... phones) {
        if (storeId == null) {
            return false;
        }
        String phone = null;
        String contact = null;
        if (phones != null) {
            List<String> normalized = new ArrayList<>();
            for (String item : phones) {
                if (!StringUtils.hasText(item)) {
                    continue;
                }
                String value = normalizePhone(item);
                if (StringUtils.hasText(value) && !normalized.contains(value)) {
                    normalized.add(value);
                }
            }
            if (!normalized.isEmpty()) {
                phone = normalized.get(0);
            }
            if (normalized.size() > 1) {
                contact = normalized.get(1);
            }
        }
        if (!StringUtils.hasText(phone) && !StringUtils.hasText(contact) && userId == null) {
            return false;
        }
        return storeBlacklistMapper.countHit(storeId, phone, contact, userId) > 0;
    }

    /**
     * 系统用户：以 userId 为主，手机号有则回填、没有也可拉黑。
     * 外部人员：不要求 userId，手机号 + 店铺即可。
     */
    private void fillTarget(StoreBlacklist record) {
        String phone = normalizePhone(record.getPhone());
        if (!StringUtils.hasText(phone)) {
            record.setPhone(null);
            phone = null;
        } else {
            record.setPhone(phone);
        }

        User target = null;
        if (record.getUserId() != null) {
            target = userMapper.selectById(record.getUserId());
            if (target == null || (target.getDeleted() != null && target.getDeleted() == 1)) {
                throw new BusinessException("用户不存在");
            }
        } else if (phone != null) {
            target = userMapper.selectByPhone(phone);
        }

        if (target != null) {
            record.setUserId(target.getId());
            if (!StringUtils.hasText(record.getRealName())) {
                record.setRealName(target.getRealName());
            }
            if (!StringUtils.hasText(record.getPhone()) && StringUtils.hasText(target.getPhone())) {
                String userPhone = normalizePhone(target.getPhone());
                record.setPhone(StringUtils.hasText(userPhone) ? userPhone : null);
            }
        }

        boolean hasUser = record.getUserId() != null;
        boolean hasPhone = StringUtils.hasText(record.getPhone());
        if (!hasUser && !hasPhone) {
            throw new BusinessException("请输入手机号或指定用户");
        }
        if (hasPhone && !record.getPhone().matches(PHONE_PATTERN)) {
            throw new BusinessException("请输入正确的手机号码");
        }
        if (!hasPhone) {
            record.setPhone(null);
        }
    }

    private StoreBlacklist findExisting(Long storeId, Long userId, String phone) {
        if (userId != null) {
            StoreBlacklist byUser = storeBlacklistMapper.selectByStoreAndUserIdAny(storeId, userId);
            if (byUser != null) {
                return byUser;
            }
        }
        if (StringUtils.hasText(phone)) {
            return storeBlacklistMapper.selectByStoreAndPhoneAny(storeId, phone);
        }
        return null;
    }

    private Long resolveWritableStoreId(Integer identityType, Long operatorId, Long requestStoreId) {
        if (identityType != null && identityType == 2) {
            Long storeId = findMerchantStoreId(operatorId);
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

    private void assertCanWriteStore(Integer identityType, Long operatorId, Long storeId) {
        if (identityType != null && identityType == 3) {
            return;
        }
        Long mine = findMerchantStoreId(operatorId);
        if (mine == null || storeId == null || !mine.equals(storeId)) {
            throw new BusinessException("无权操作其他店铺的黑名单");
        }
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

    private Claims parseClaims(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
            throw new BusinessException("未登录");
        }
        return jwtUtil.parseToken(auth.substring(7));
    }

    private String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return "";
        }
        String value = phone.trim().replace(" ", "");
        if (value.startsWith("+86")) {
            value = value.substring(3);
        } else if (value.startsWith("86") && value.length() == 13) {
            value = value.substring(2);
        }
        return value;
    }

    private Response buildPageResponse(List<StoreBlacklist> list, Integer pageNum, Integer pageSize) {
        PageInfo<StoreBlacklist> pageInfo = new PageInfo<>(list);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getList());
        data.put("total", pageInfo.getTotal());
        data.put("pages", pageInfo.getPages());
        data.put("pageNum", pageNum != null ? pageInfo.getPageNum() : 1);
        data.put("pageSize", pageSize != null ? pageInfo.getPageSize() : pageInfo.getTotal());
        return new Response(200, data, "操作成功");
    }
}
