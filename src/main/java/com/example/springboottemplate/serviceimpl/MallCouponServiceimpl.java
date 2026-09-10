package com.example.springboottemplate.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.MallCouponTemplate;
import com.example.springboottemplate.entity.MallUserCoupon;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.MallCouponTemplateMapper;
import com.example.springboottemplate.mapper.MallUserCouponMapper;
import com.example.springboottemplate.mapper.system.UserMapper;
import com.example.springboottemplate.service.MallCouponService;
import com.example.springboottemplate.utils.LogicDeleteHelper;
import com.example.springboottemplate.utils.MallDiscountCalculator;
import com.example.springboottemplate.utils.StoreAuthHelper;
import com.example.springboottemplate.utils.StoreAuthHelper.Operator;
import com.example.springboottemplate.utils.ValidateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class MallCouponServiceimpl implements MallCouponService {

    @Autowired
    private MallCouponTemplateMapper mallCouponTemplateMapper;
    @Autowired
    private MallUserCouponMapper mallUserCouponMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StoreAuthHelper storeAuthHelper;
    @Autowired
    private LogicDeleteHelper logicDeleteHelper;

    @Override
    public Response addMallCouponTemplate(MallCouponTemplate record, HttpServletRequest request) {
        Operator operator = storeAuthHelper.requireMerchantOrAdmin(request);
        if (record == null) {
            throw new BusinessException("参数不能为空");
        }
        Long storeId = storeAuthHelper.resolveWritableStoreId(operator, record.getStoreId());
        record.setStoreId(storeId);
        normalizeTemplate(record, true);
        if (record.getStatus() == null) {
            record.setStatus(1);
        }
        record.setId(IdWorker.getId());
        record.setIssuedCount(0);
        record.setDeleted(0);
        record.setCreatedBy(operator.getUsername());
        record.setCreatedTime(new Date());
        mallCouponTemplateMapper.addMallCouponTemplate(record);
        Map<String, Object> extra = grantIfRequested(record);
        if (extra == null) {
            return Response.success();
        }
        return Response.success(extra);
    }

    @Override
    public Response findMallCouponTemplate(MallCouponTemplate record, Integer pageNum, Integer pageSize,
                                           HttpServletRequest request) {
        Operator operator = storeAuthHelper.requireMerchantOrAdmin(request);
        if (record == null) {
            record = new MallCouponTemplate();
        }
        Long storeId = storeAuthHelper.applyListStoreScope(operator, record.getStoreId());
        if (operator.isMerchant() && storeId == null) {
            return buildPageResponse(Collections.emptyList(), pageNum, pageSize);
        }
        record.setStoreId(storeId);
        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        List<MallCouponTemplate> list = mallCouponTemplateMapper.findMallCouponTemplate(record);
        Date now = new Date();
        for (MallCouponTemplate item : list) {
            item.setExpireText(MallDiscountCalculator.expireText(item.getForever(), item.getEndTime()));
            boolean on = item.getStatus() != null && item.getStatus() == 1
                    && MallDiscountCalculator.isEffective(item.getForever(), item.getEndTime(), now);
            item.setEffective(on);
            storeAuthHelper.fillStoreName(item.getStoreId(), item::setStoreName);
        }
        return buildPageResponse(list, pageNum, pageSize);
    }

    @Override
    public Response updateMallCouponTemplate(MallCouponTemplate record, HttpServletRequest request) {
        Operator operator = storeAuthHelper.requireMerchantOrAdmin(request);
        if (record == null || record.getId() == null) {
            throw new BusinessException("记录ID不能为空");
        }
        MallCouponTemplate exist = mallCouponTemplateMapper.selectTemplateById(record.getId());
        if (exist == null) {
            throw new BusinessException("优惠券不存在");
        }
        storeAuthHelper.assertCanWriteStore(operator, exist.getStoreId());
        if (record.getName() != null) {
            normalizeTemplate(record, false);
        }
        record.setUpdateBy(operator.getUsername());
        mallCouponTemplateMapper.updateMallCouponTemplate(record);
        return Response.success();
    }

    @Override
    public Response deleteMallCouponTemplate(List<Long> idList, HttpServletRequest request) {
        Operator operator = storeAuthHelper.requireMerchantOrAdmin(request);
        if (ValidateUtil.isEmpty(idList)) {
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        List<Long> allowed = new ArrayList<>();
        for (Long id : idList) {
            if (id == null) {
                continue;
            }
            MallCouponTemplate exist = mallCouponTemplateMapper.selectTemplateById(id);
            if (exist == null) {
                continue;
            }
            try {
                storeAuthHelper.assertCanWriteStore(operator, exist.getStoreId());
                allowed.add(id);
            } catch (BusinessException ignored) {
                // skip
            }
        }
        if (allowed.isEmpty()) {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
        int affected = logicDeleteHelper.deleteByIds("mall_coupon_template", allowed);
        return affected > 0 ? Response.success() : new Response(400, null, "操作失败，未找到需要删除的记录");
    }

    @Override
    public MallUserCoupon requireUsableCoupon(Long userCouponId, Long userId, Long storeId) {
        if (userCouponId == null) {
            return null;
        }
        MallUserCoupon coupon = mallUserCouponMapper.selectCouponById(userCouponId);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        if (userId == null || !userId.equals(coupon.getUserId())) {
            throw new BusinessException("优惠券不属于当前用户");
        }
        if (storeId == null || !storeId.equals(coupon.getStoreId())) {
            throw new BusinessException("优惠券不属于当前店铺");
        }
        if (coupon.getStatus() == null || coupon.getStatus() != 0) {
            throw new BusinessException("优惠券不可用");
        }
        if (!MallDiscountCalculator.isEffective(coupon.getForever(), coupon.getExpireTime(), new Date())) {
            throw new BusinessException("优惠券已过期");
        }
        return coupon;
    }

    @Override
    public boolean lockCoupon(Long userCouponId, Long userId, String orderNo) {
        if (userCouponId == null || userId == null || !StringUtils.hasText(orderNo)) {
            return true;
        }
        return mallUserCouponMapper.lockCoupon(userCouponId, userId, orderNo, new Date()) > 0;
    }

    @Override
    public void unlockByOrderNo(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            return;
        }
        mallUserCouponMapper.unlockByOrderNo(orderNo);
    }

    @Override
    public void markUsedByOrderNo(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            return;
        }
        mallUserCouponMapper.markUsedByOrderNo(orderNo, new Date());
    }

    @Override
    public Response listStoreTemplates(Long storeId, HttpServletRequest request) {
        if (storeId == null) {
            throw new BusinessException("店铺不能为空");
        }
        Date now = new Date();
        List<MallCouponTemplate> list = mallCouponTemplateMapper.selectClaimableByStoreId(storeId, now);
        if (list == null) {
            list = Collections.emptyList();
        }
        Long userId = storeAuthHelper.tryGetUserId(request);
        for (MallCouponTemplate item : list) {
            item.setExpireText(MallDiscountCalculator.expireText(item.getForever(), item.getEndTime()));
            item.setEffective(true);
            Integer remain = null;
            if (item.getTotalCount() != null) {
                int issued = item.getIssuedCount() == null ? 0 : item.getIssuedCount();
                remain = Math.max(0, item.getTotalCount() - issued);
            }
            item.setRemainCount(remain);
            boolean claimed = false;
            if (userId != null) {
                int owned = mallUserCouponMapper.countByTemplateAndUser(item.getId(), userId);
                int limit = item.getPerUserLimit() == null ? 1 : item.getPerUserLimit();
                claimed = owned >= limit;
            }
            item.setClaimed(claimed);
            storeAuthHelper.fillStoreName(item.getStoreId(), item::setStoreName);
        }
        return Response.success(list);
    }

    @Override
    public Response receiveCoupon(Long templateId, HttpServletRequest request) {
        if (templateId == null) {
            throw new BusinessException("请选择优惠券");
        }
        User user = storeAuthHelper.requireLoginUser(request);
        MallCouponTemplate template = mallCouponTemplateMapper.selectTemplateById(templateId);
        if (template == null) {
            throw new BusinessException("优惠券不存在");
        }
        if (template.getStatus() == null || template.getStatus() != 1) {
            throw new BusinessException("优惠券已停止领取");
        }
        Date now = new Date();
        if (!MallDiscountCalculator.isEffective(template.getForever(), template.getEndTime(), now)) {
            throw new BusinessException("优惠券已过期");
        }
        if (isGrantOnly(template)) {
            throw new BusinessException("该优惠券由商家发放，无法自行领取");
        }
        if (user.getIdentityType() != null && user.getIdentityType() == 2) {
            Long mine = storeAuthHelper.findMerchantStoreId(user.getId());
            if (mine != null && mine.equals(template.getStoreId())) {
                throw new BusinessException("商家不能领取本店优惠券");
            }
        }
        String fail = issueToUser(template, user.getId(), now);
        if (fail != null) {
            throw new BusinessException("已达限领".equals(fail) ? "已领取过该优惠券" : fail);
        }
        return Response.success();
    }

    @Override
    public Response findMyCoupons(String tab, Long storeId, Integer pageNum, Integer pageSize,
                                  HttpServletRequest request) {
        User user = storeAuthHelper.requireLoginUser(request);
        String normalized = normalizeCouponTab(tab);
        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        List<MallUserCoupon> list = mallUserCouponMapper.findMallUserCoupon(
                user.getId(), storeId, normalized, new Date());
        for (MallUserCoupon item : list) {
            item.setExpireText(MallDiscountCalculator.expireText(item.getForever(), item.getExpireTime()));
            storeAuthHelper.fillStoreName(item.getStoreId(), item::setStoreName);
        }
        PageInfo<MallUserCoupon> pageInfo = new PageInfo<>(list);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getList());
        data.put("total", pageInfo.getTotal());
        data.put("pages", pageInfo.getPages());
        data.put("pageNum", pageNum != null ? pageInfo.getPageNum() : 1);
        data.put("pageSize", pageSize != null ? pageInfo.getPageSize() : pageInfo.getTotal());
        data.put("tab", normalized);
        return new Response(200, data, "操作成功");
    }

    @Override
    public List<MallUserCoupon> listUnusedByUserAndStore(Long userId, Long storeId) {
        if (userId == null || storeId == null) {
            return Collections.emptyList();
        }
        List<MallUserCoupon> list = mallUserCouponMapper.selectUsableByUserAndStore(userId, storeId, new Date());
        if (list == null) {
            return Collections.emptyList();
        }
        for (MallUserCoupon item : list) {
            item.setExpireText(MallDiscountCalculator.expireText(item.getForever(), item.getExpireTime()));
            storeAuthHelper.fillStoreName(item.getStoreId(), item::setStoreName);
        }
        return list;
    }

    @Override
    public Response grantCoupon(Long templateId, List<Long> userIds, HttpServletRequest request) {
        Operator operator = storeAuthHelper.requireMerchantOrAdmin(request);
        if (templateId == null) {
            throw new BusinessException("请选择优惠券");
        }
        MallCouponTemplate template = mallCouponTemplateMapper.selectTemplateById(templateId);
        if (template == null) {
            throw new BusinessException("优惠券不存在");
        }
        storeAuthHelper.assertCanWriteStore(operator, template.getStoreId());
        if (!isGrantOnly(template)) {
            throw new BusinessException("顾客领取券请让用户自行领取");
        }
        assertTemplateGrantable(template, new Date());
        List<Long> ids = uniqueIds(userIds);
        if (ids.isEmpty()) {
            throw new BusinessException("请选择要发放的用户");
        }
        return Response.success(doGrant(template, ids));
    }

    private Map<String, Object> grantIfRequested(MallCouponTemplate record) {
        if (!isGrantOnly(record) || ValidateUtil.isEmpty(record.getUserIds())) {
            return null;
        }
        List<Long> ids = new ArrayList<>();
        for (Object raw : record.getUserIds()) {
            Long id = parseLongId(raw);
            if (id != null) {
                ids.add(id);
            }
        }
        ids = uniqueIds(ids);
        if (ids.isEmpty()) {
            return null;
        }
        return doGrant(record, ids);
    }

    private Map<String, Object> doGrant(MallCouponTemplate template, List<Long> userIds) {
        Date now = new Date();
        int granted = 0;
        int skipped = 0;
        for (Long userId : userIds) {
            User target = userMapper.selectById(userId);
            String fail = validateGrantTarget(template, target);
            if (fail != null) {
                skipped++;
                continue;
            }
            String issueFail = issueToUser(template, target.getId(), now);
            if (issueFail == null) {
                granted++;
                continue;
            }
            skipped++;
            if ("优惠券已发完".equals(issueFail)) {
                break;
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("granted", granted);
        data.put("skipped", skipped);
        data.put("message", "成功发放" + granted + "张"
                + (skipped > 0 ? "，跳过" + skipped + "人" : ""));
        return data;
    }

    private String validateGrantTarget(MallCouponTemplate template, User target) {
        if (target == null) {
            return "用户不存在";
        }
        if (target.getIdentityType() == null || target.getIdentityType() != 1) {
            return "只能发给普通用户";
        }
        if (target.getBindStoreId() == null || !target.getBindStoreId().equals(template.getStoreId())) {
            return "用户不属于该店铺";
        }
        return null;
    }

    private void assertTemplateGrantable(MallCouponTemplate template, Date now) {
        if (template.getStatus() == null || template.getStatus() != 1) {
            throw new BusinessException("优惠券已停用");
        }
        if (!MallDiscountCalculator.isEffective(template.getForever(), template.getEndTime(), now)) {
            throw new BusinessException("优惠券已过期");
        }
    }

    private String issueToUser(MallCouponTemplate template, Long userId, Date now) {
        int owned = mallUserCouponMapper.countByTemplateAndUser(template.getId(), userId);
        int limit = template.getPerUserLimit() == null ? 1 : template.getPerUserLimit();
        if (owned >= limit) {
            return "已达限领";
        }
        int increased = mallCouponTemplateMapper.increaseIssuedCount(template.getId(), template.getTotalCount());
        if (increased <= 0) {
            return "优惠券已发完";
        }
        mallUserCouponMapper.addMallUserCoupon(MallUserCoupon.builder()
                .id(IdWorker.getId())
                .templateId(template.getId())
                .storeId(template.getStoreId())
                .userId(userId)
                .name(template.getName())
                .thresholdAmount(template.getThresholdAmount())
                .discountAmount(template.getDiscountAmount())
                .status(0)
                .forever(template.getForever())
                .expireTime(Integer.valueOf(1).equals(template.getForever()) ? null : template.getEndTime())
                .receiveTime(now)
                .deleted(0)
                .build());
        return null;
    }

    private boolean isGrantOnly(MallCouponTemplate template) {
        return template != null && Integer.valueOf(2).equals(template.getIssueType());
    }

    private List<Long> uniqueIds(List<Long> userIds) {
        if (ValidateUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return new ArrayList<>(new LinkedHashSet<>(userIds));
    }

    private Long parseLongId(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number) {
            return ((Number) raw).longValue();
        }
        String text = String.valueOf(raw).trim();
        if (text.isEmpty() || "null".equals(text)) {
            return null;
        }
        try {
            return Long.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalizeCouponTab(String tab) {
        if ("used".equals(tab) || "expired".equals(tab)) {
            return tab;
        }
        return "unused";
    }

    private void normalizeTemplate(MallCouponTemplate record, boolean creating) {
        if (!StringUtils.hasText(record.getName())) {
            throw new BusinessException("请输入优惠券名称");
        }
        record.setName(record.getName().trim());
        if (record.getName().length() > 64) {
            throw new BusinessException("优惠券名称不能超过64个字");
        }
        BigDecimal threshold = record.getThresholdAmount() == null
                ? BigDecimal.ZERO
                : record.getThresholdAmount().setScale(2, RoundingMode.HALF_UP);
        if (threshold.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("使用门槛不能小于0");
        }
        record.setThresholdAmount(threshold);
        if (record.getDiscountAmount() == null) {
            throw new BusinessException("请输入减免金额");
        }
        BigDecimal discount = record.getDiscountAmount().setScale(2, RoundingMode.HALF_UP);
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("减免金额必须大于0");
        }
        record.setDiscountAmount(discount);
        if (record.getTotalCount() != null && record.getTotalCount() <= 0) {
            record.setTotalCount(null);
        }
        Integer perUser = record.getPerUserLimit() == null ? 1 : record.getPerUserLimit();
        if (perUser < 1) {
            throw new BusinessException("每人限领至少为1");
        }
        record.setPerUserLimit(perUser);
        Integer issueType = record.getIssueType() == null ? 1 : record.getIssueType();
        if (issueType != 1 && issueType != 2) {
            throw new BusinessException("发放方式无效");
        }
        record.setIssueType(issueType);
        Integer forever = record.getForever() == null ? 1 : record.getForever();
        if (forever != 0 && forever != 1) {
            throw new BusinessException("有效期类型无效");
        }
        record.setForever(forever);
        if (forever == 1) {
            record.setEndTime(null);
            return;
        }
        if (record.getEndTime() == null) {
            throw new BusinessException("请选择过期时间");
        }
        if (creating && !record.getEndTime().after(new Date())) {
            throw new BusinessException("过期时间必须晚于当前时间");
        }
    }

    private Response buildPageResponse(List<MallCouponTemplate> list, Integer pageNum, Integer pageSize) {
        PageInfo<MallCouponTemplate> pageInfo = new PageInfo<>(list);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getList());
        data.put("total", pageInfo.getTotal());
        data.put("pages", pageInfo.getPages());
        data.put("pageNum", pageNum != null ? pageInfo.getPageNum() : 1);
        data.put("pageSize", pageSize != null ? pageInfo.getPageSize() : pageInfo.getTotal());
        return new Response(200, data, "操作成功");
    }
}
