package com.example.springboottemplate.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.StoreBusinessHours;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.mapper.system.UserMapper;
import com.example.springboottemplate.service.StoreService;
import com.example.springboottemplate.utils.JwtUtil;
import com.example.springboottemplate.utils.LogicDeleteHelper;
import com.example.springboottemplate.utils.MiniappUiHelper;
import com.example.springboottemplate.utils.StoreAuthHelper;
import com.example.springboottemplate.utils.StoreExpireHelper;
import com.example.springboottemplate.utils.StoreOpenHelper;
import com.example.springboottemplate.utils.ValidateUtil;
import org.springframework.util.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StoreServiceimpl implements StoreService {
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private LogicDeleteHelper logicDeleteHelper;

    @Autowired
    private JwtUtil jwtUtil;  // 注入 JwtUtil
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StoreAuthHelper storeAuthHelper;

    @Override
    public Response addStore(Store store, HttpServletRequest request) {
        // 1. 从请求头中获取JWT令牌
        String token = request.getHeader("Authorization").substring(7);
        // 2. 解析令牌获取用户名
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        // 3. 系统生成雪花 id / storeId，忽略前端传入
        store.setId(IdWorker.getId());
        store.setStoreId(IdWorker.getId());
        store.setCreatedTime(new Date());
        store.setCreatedBy(username);
        if (store.getStoreStatus() == null) {
            store.setStoreStatus(1); // 默认正常/营业
        }
        if (store.getDeleted() == null) {
            store.setDeleted(0);
        }
        MiniappUiHelper.fillDefaults(store);
        Response subscriptionError = applySubscriptionFields(store, true);
        if (subscriptionError != null) {
            return subscriptionError;
        }
        applyLifeStatusFields(store, null, true);

        storeMapper.addStore(store);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findStore(Store store, Integer pageNum, Integer pageSize) {
        // 传了分页参数才开启分页，否则返回全部数据
        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        // 查询数据
        List<Store> list = storeMapper.findStore(store);
        // 添加自定义userName和realName字段
        list.forEach(item -> {
            User user = item.getUserId() == null ? null : userMapper.selectById(item.getUserId());
            item.setUserName(user != null ? user.getUserName() : null);
            item.setRealName(user != null ? user.getRealName() : null);
            fillOpenFields(item);
        });
        // 封装分页结果
        PageInfo<Store> pageInfo = new PageInfo<>(list);
        // 构造返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);  // 当前页数据
        data.put("total", pageInfo.getTotal()); // 总记录数
        data.put("pages", pageInfo.getPages()); // 总页数
        data.put("pageNum", pageInfo.getPageNum()); // 当前页码
        data.put("pageSize", pageInfo.getPageSize()); // 每页数量
        return new Response(200, data, "操作成功");
    }

    @Override
    public Response updateStore(Store store, HttpServletRequest request) {
        if (store == null || store.getId() == null) {
            return Response.fail(400, "缺少店铺ID");
        }
        String token = request.getHeader("Authorization").substring(7);
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        Store exist = findById(store.getId());
        MiniappUiHelper.fillDefaults(exist);
        if (store.getDeliveryFee() != null) {
            if (!MiniappUiHelper.showDeliveryFee(exist)) {
                store.setDeliveryFee(null);
            } else if (store.getDeliveryFee().compareTo(BigDecimal.ZERO) < 0) {
                return Response.fail(400, "配送费不能小于0");
            } else {
                store.setDeliveryFee(store.getDeliveryFee().setScale(2, RoundingMode.HALF_UP));
            }
        }
        String hoursJson = store.getBusinessHours() != null
                ? store.getBusinessHours()
                : (exist == null ? null : exist.getBusinessHours());
        applyStatusChangeIfNeeded(store, exist, hoursJson);
        Response subscriptionError = applySubscriptionFields(store, false);
        if (subscriptionError != null) {
            return subscriptionError;
        }
        applyLifeStatusFields(store, exist, false);
        store.setUpdateBy(username);
        storeMapper.updateStore(store);
        return Response.success(refreshStore(store.getId(), exist != null ? exist : store));
    }

    @Override
    public Response updateStoreProfile(Store body, HttpServletRequest request) {
        Long userId = resolveCurrentUserId(request);
        if (userId == null) {
            return Response.fail(401, "无效的登录信息");
        }
        if (body == null || !org.springframework.util.StringUtils.hasText(body.getStoreName())) {
            return Response.fail(400, "店铺名称不能为空");
        }
        String storeName = body.getStoreName().trim();
        if (storeName.length() > 64) {
            return Response.fail(400, "店铺名称不能超过64个字符");
        }

        Store exist = findCurrentOwnedStore(userId, request);
        if (exist == null || exist.getId() == null) {
            return Response.fail(400, "未找到店铺信息");
        }

        String address = body.getAddress();
        if (address != null) {
            address = address.trim();
            if (address.length() > 255) {
                return Response.fail(400, "店铺位置不能超过255个字符");
            }
        }
        String avatar = body.getAvatar();
        if (avatar != null) {
            avatar = avatar.trim();
            if (avatar.length() > 255) {
                return Response.fail(400, "店铺照片路径过长");
            }
        }

        String operator = exist.getCreatedBy();
        Object usernameAttr = request.getAttribute("username");
        if (usernameAttr instanceof String && org.springframework.util.StringUtils.hasText((String) usernameAttr)) {
            operator = (String) usernameAttr;
        }

        Store patch = new Store();
        patch.setId(exist.getId());
        patch.setStoreName(storeName);
        patch.setAddress(address);
        patch.setAvatar(avatar);
        patch.setUpdateBy(operator);
        storeMapper.updateStore(patch);

        return Response.success(refreshStore(exist.getId(), exist));
    }

    @Override
    public Response updateStoreIntro(Store body, HttpServletRequest request) {
        try {
            StoreAuthHelper.Operator operator = storeAuthHelper.requireMerchantOrAdmin(request);
            if (body == null) {
                return Response.fail(400, "参数不能为空");
            }
            Long storeId = storeAuthHelper.resolveWritableStoreId(operator, body.getStoreId());
            Store exist = storeMapper.selectByStoreId(storeId);
            if (exist == null || (exist.getDeleted() != null && exist.getDeleted() == 1)) {
                return Response.fail(400, "未找到店铺信息");
            }
            if (!StringUtils.hasText(body.getStoreName())) {
                return Response.fail(400, "店铺名称不能为空");
            }
            String storeName = body.getStoreName().trim();
            if (storeName.length() > 64) {
                return Response.fail(400, "店铺名称不能超过64个字符");
            }
            String intro = body.getIntro() == null ? "" : body.getIntro().trim();
            if (intro.length() > 500) {
                return Response.fail(400, "店铺简介不能超过500个字符");
            }
            String notice = body.getActivityNotice() == null ? "" : body.getActivityNotice().trim();
            if (notice.length() > 200) {
                return Response.fail(400, "活动公告不能超过200个字符");
            }
            String phone = body.getContactPhone() == null
                    ? ""
                    : body.getContactPhone().replaceAll("\\s+", "").trim();
            if (StringUtils.hasText(phone) && !phone.matches("^[0-9+\\-]{5,20}$")) {
                return Response.fail(400, "请输入正确的联系电话");
            }
            String avatar = body.getAvatar();
            if (avatar != null) {
                avatar = avatar.trim();
                if (avatar.length() > 255) {
                    return Response.fail(400, "店铺照片路径过长");
                }
            }

            Store patch = new Store();
            patch.setId(exist.getId());
            patch.setStoreName(storeName);
            patch.setIntro(intro);
            patch.setActivityNotice(notice);
            patch.setContactPhone(phone);
            patch.setShowHours(normalizeIntroFlag(body.getShowHours()));
            patch.setShowActivity(normalizeIntroFlag(body.getShowActivity()));
            patch.setShowContact(normalizeIntroFlag(body.getShowContact()));
            patch.setShowOtherBusiness(normalizeIntroFlag(body.getShowOtherBusiness()));
            patch.setUpdateBy(operator.getUsername());
            if (avatar != null) {
                patch.setAvatar(avatar);
            }
            storeMapper.updateStore(patch);
            return Response.success(refreshStore(exist.getId(), exist));
        } catch (BusinessException e) {
            return Response.fail(400, e.getMessage());
        }
    }

    private Integer normalizeIntroFlag(Integer value) {
        if (value == null) {
            return 1;
        }
        return value == 1 ? 1 : 0;
    }

    @Override
    public Response updateBusinessHours(StoreBusinessHours hours, HttpServletRequest request) {
        Long userId = resolveCurrentUserId(request);
        if (userId == null) {
            return Response.fail(401, "无效的登录信息");
        }
        User current = userMapper.selectById(userId);
        Integer identityType = current == null ? null : current.getIdentityType();

        Store exist;
        if (identityType != null && identityType == 3) {
            Long targetStoreId = hours == null ? null : hours.getStoreId();
            if (targetStoreId == null) {
                return Response.fail(400, "请选择店铺");
            }
            exist = storeMapper.selectByStoreId(targetStoreId);
            if (exist == null || (exist.getDeleted() != null && exist.getDeleted() == 1)) {
                return Response.fail(400, "未找到店铺信息");
            }
        } else {
            exist = findCurrentOwnedStore(userId, request);
            if (exist == null) {
                return Response.fail(400, "未找到店铺信息");
            }
        }

        String json;
        try {
            json = StoreOpenHelper.normalizeToJson(hours);
        } catch (IllegalArgumentException e) {
            return Response.fail(400, e.getMessage());
        }

        String operator = exist.getCreatedBy();
        Object usernameAttr = request.getAttribute("username");
        if (usernameAttr instanceof String && org.springframework.util.StringUtils.hasText((String) usernameAttr)) {
            operator = (String) usernameAttr;
        }

        Store patch = new Store();
        patch.setId(exist.getId());
        patch.setBusinessHours(json);
        patch.setUpdateBy(operator);
        exist.setBusinessHours(json);
        ZonedDateTime now = ZonedDateTime.now(StoreOpenHelper.SHANGHAI);
        if (StoreOpenHelper.isManuallyClosed(exist, now)) {
            applyManualStatus(patch, json, StoreOpenHelper.STORE_STATUS_CLOSED);
        } else if (StoreOpenHelper.isForcedOpen(exist, now)) {
            applyManualStatus(patch, json, StoreOpenHelper.STORE_STATUS_OPEN);
        }
        storeMapper.updateStore(patch);

        return Response.success(refreshStore(exist.getId(), exist));
    }

    private void applyStatusChangeIfNeeded(Store patch, Store exist, String businessHoursJson) {
        if (patch == null || patch.getStoreStatus() == null) {
            return;
        }
        int target = patch.getStoreStatus();
        if (target == StoreOpenHelper.STORE_STATUS_CLOSED
                || target == StoreOpenHelper.STORE_STATUS_OPEN) {
            applyManualStatus(patch, businessHoursJson, target);
        }
    }

    private void applyManualStatus(Store patch, String businessHoursJson, Integer storeStatus) {
        if (patch == null || storeStatus == null) {
            return;
        }
        ZonedDateTime now = ZonedDateTime.now(StoreOpenHelper.SHANGHAI);
        if (storeStatus == StoreOpenHelper.STORE_STATUS_CLOSED) {
            patch.setOpenUntil(null);
            patch.setOpenUntilCleared(true);
            ZonedDateTime nextOpen = StoreOpenHelper.nextOpenTime(businessHoursJson, now);
            if (nextOpen != null) {
                patch.setClosedUntil(StoreOpenHelper.toDate(nextOpen));
                patch.setClosedUntilCleared(false);
            } else {
                patch.setClosedUntil(null);
                patch.setClosedUntilCleared(true);
            }
            return;
        }
        patch.setClosedUntil(null);
        patch.setClosedUntilCleared(true);
        ZonedDateTime nextClose = StoreOpenHelper.nextCloseTime(businessHoursJson, now);
        if (nextClose != null) {
            patch.setOpenUntil(StoreOpenHelper.toDate(nextClose));
            patch.setOpenUntilCleared(false);
        } else {
            patch.setOpenUntil(null);
            patch.setOpenUntilCleared(true);
        }
    }

    private Store findById(Long id) {
        if (id == null) {
            return null;
        }
        Store query = new Store();
        query.setId(id);
        query.setDeleted(0);
        List list = storeMapper.findStore(query);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return (Store) list.get(0);
    }

    private Store findCurrentOwnedStore(Long userId, HttpServletRequest request) {
        Long storeId = storeAuthHelper.resolveCurrentOwnedStoreId(
                userId, storeAuthHelper.parseHeaderStoreId(request));
        if (storeId == null) {
            return null;
        }
        Store exist = storeMapper.selectByStoreId(storeId);
        if (exist == null || exist.getId() == null || (exist.getDeleted() != null && exist.getDeleted() == 1)) {
            return null;
        }
        return exist;
    }

    private Store refreshStore(Long id, Store fallback) {
        Store refreshQuery = new Store();
        refreshQuery.setId(id);
        refreshQuery.setDeleted(0);
        List refreshedList = storeMapper.findStore(refreshQuery);
        Store refreshed = (refreshedList != null && !refreshedList.isEmpty())
                ? (Store) refreshedList.get(0)
                : fallback;
        fillOpenFields(refreshed);
        return refreshed;
    }

    private void fillOpenFields(Store store) {
        if (store == null) {
            return;
        }
        ZonedDateTime now = ZonedDateTime.now(StoreOpenHelper.SHANGHAI);
        store.setManuallyClosed(StoreOpenHelper.isManuallyClosed(store, now));
        store.setAcceptingOrders(StoreOpenHelper.isAcceptingOrders(store, now));
        store.setOpenStatus(StoreOpenHelper.openStatus(store, now));
        store.setBusinessHoursText(StoreOpenHelper.formatText(store.getBusinessHours()));
        store.setStatusHint(StoreOpenHelper.statusHint(store, now));
        store.setClosedUntilText(StoreOpenHelper.closedUntilText(store, now));
        store.setOpenUntilText(StoreOpenHelper.openUntilText(store, now));
        store.setNextOpenText(StoreOpenHelper.formatMoment(
                StoreOpenHelper.nextOpenTime(store.getBusinessHours(), now), now));
        store.setNextCloseText(StoreOpenHelper.formatMoment(
                StoreOpenHelper.nextCloseTime(store.getBusinessHours(), now), now));
        store.setSubscriptionExpired(StoreExpireHelper.isExpired(store));
        MiniappUiHelper.fillDefaults(store);
    }

    private Response applySubscriptionFields(Store store, boolean creating) {
        if (store == null) {
            return Response.fail(400, "参数不能为空");
        }
        Integer type = store.getStoreType();
        if (creating && type == null) {
            return Response.fail(400, "请选择商户类型");
        }
        if (type == null) {
            return null;
        }
        if (type == StoreExpireHelper.TYPE_FOREVER) {
            store.setStoreTime(null);
            store.setStoreTimeCleared(true);
            return null;
        }
        if (type == StoreExpireHelper.TYPE_RENT) {
            String normalized = StoreExpireHelper.normalizeStoreTime(store.getStoreTime());
            if (!StringUtils.hasText(normalized)) {
                return Response.fail(400, "租用商户请选择到期时间");
            }
            store.setStoreTime(normalized);
            store.setStoreTimeCleared(false);
            return null;
        }
        return Response.fail(400, "商户类型无效");
    }

    private void applyLifeStatusFields(Store store, Store exist, boolean creating) {
        if (store == null) {
            return;
        }
        Store snapshot = new Store();
        snapshot.setStoreType(store.getStoreType() != null
                ? store.getStoreType()
                : (exist == null ? null : exist.getStoreType()));
        if (Boolean.TRUE.equals(store.getStoreTimeCleared())) {
            snapshot.setStoreTime(null);
        } else if (store.getStoreTime() != null) {
            snapshot.setStoreTime(store.getStoreTime());
        } else {
            snapshot.setStoreTime(exist == null ? null : exist.getStoreTime());
        }
        int expiredFlag = StoreExpireHelper.expiredFlag(snapshot);
        boolean subscriptionChanged = creating
                || store.getStoreType() != null
                || store.getStoreTime() != null
                || Boolean.TRUE.equals(store.getStoreTimeCleared());
        if (creating) {
            if (store.getExpired() == null) {
                store.setExpired(expiredFlag);
            }
        } else if (store.getExpired() == null && subscriptionChanged) {
            store.setExpired(expiredFlag);
        }
        Integer deleted = store.getDeleted() != null
                ? store.getDeleted()
                : (exist == null ? Integer.valueOf(0) : exist.getDeleted());
        boolean unlocking = store.getDeleted() != null && store.getDeleted() == 0;
        if (creating) {
            if (store.getShopStatus() == null) {
                store.setShopStatus(StoreExpireHelper.resolveShopStatus(
                        deleted,
                        store.getExpired() == null ? expiredFlag : store.getExpired()));
            }
            return;
        }
        if (Integer.valueOf(1).equals(deleted)) {
            store.setShopStatus(StoreExpireHelper.SHOP_STATUS_FROZEN);
            return;
        }
        if (unlocking && store.getShopStatus() == null) {
            store.setShopStatus(StoreExpireHelper.resolveShopStatus(
                    0,
                    store.getExpired() == null ? expiredFlag : store.getExpired()));
        }
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

    @Override
    public Response deleteStore(List<Long> idList) {
        if (ValidateUtil.isEmpty(idList)) {  // 使用工具类
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        int affectedRows = logicDeleteHelper.deleteByIds("store", idList);
        if (affectedRows > 0) {
            for (Long id : idList) {
                if (id == null) {
                    continue;
                }
                Store patch = new Store();
                patch.setId(id);
                patch.setShopStatus(StoreExpireHelper.SHOP_STATUS_FROZEN);
                storeMapper.updateStore(patch);
            }
            return new Response(200, null, "操作成功");
        } else {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
    }
}
