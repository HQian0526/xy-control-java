package com.example.springboottemplate.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.MallPromo;
import com.example.springboottemplate.entity.MallPromoTier;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.MallPromoMapper;
import com.example.springboottemplate.mapper.MallPromoTierMapper;
import com.example.springboottemplate.service.MallPromoService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class MallPromoServiceimpl implements MallPromoService {

    @Autowired
    private MallPromoMapper mallPromoMapper;
    @Autowired
    private MallPromoTierMapper mallPromoTierMapper;
    @Autowired
    private StoreAuthHelper storeAuthHelper;
    @Autowired
    private LogicDeleteHelper logicDeleteHelper;

    @Override
    public Response addMallPromo(MallPromo record, HttpServletRequest request) {
        Operator operator = storeAuthHelper.requireMerchantOrAdmin(request);
        if (record == null) {
            throw new BusinessException("参数不能为空");
        }
        Long storeId = storeAuthHelper.resolveWritableStoreId(operator, record.getStoreId());
        record.setStoreId(storeId);
        normalizeExpire(record, true);
        List<MallPromoTier> tiers = validateTiers(record.getTiers());
        if (record.getStatus() == null) {
            record.setStatus(1);
        }
        assertSingleActive(storeId, null, record);
        Date now = new Date();
        record.setId(IdWorker.getId());
        record.setDeleted(0);
        record.setCreatedBy(operator.getUsername());
        record.setCreatedTime(now);
        mallPromoMapper.addMallPromo(record);
        saveTiers(record.getId(), tiers);
        return Response.success();
    }

    @Override
    public Response findMallPromo(MallPromo record, Integer pageNum, Integer pageSize, HttpServletRequest request) {
        Operator operator = storeAuthHelper.requireMerchantOrAdmin(request);
        if (record == null) {
            record = new MallPromo();
        }
        Long storeId = storeAuthHelper.applyListStoreScope(operator, record.getStoreId());
        if (operator.isMerchant() && storeId == null) {
            return buildPageResponse(Collections.emptyList(), pageNum, pageSize);
        }
        record.setStoreId(storeId);
        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        List<MallPromo> list = mallPromoMapper.findMallPromo(record);
        fillTiersAndDisplay(list);
        return buildPageResponse(list, pageNum, pageSize);
    }

    @Override
    public Response updateMallPromo(MallPromo record, HttpServletRequest request) {
        Operator operator = storeAuthHelper.requireMerchantOrAdmin(request);
        if (record == null || record.getId() == null) {
            throw new BusinessException("记录ID不能为空");
        }
        MallPromo exist = mallPromoMapper.selectPromoById(record.getId());
        if (exist == null) {
            throw new BusinessException("活动不存在");
        }
        storeAuthHelper.assertCanWriteStore(operator, exist.getStoreId());
        if (record.getName() != null) {
            normalizeExpire(record, false);
        }
        Integer nextStatus = record.getStatus() != null ? record.getStatus() : exist.getStatus();
        Integer nextForever = record.getForever() != null ? record.getForever() : exist.getForever();
        Date nextEnd = record.getForever() != null
                ? (Integer.valueOf(1).equals(record.getForever()) ? null : record.getEndTime())
                : exist.getEndTime();
        MallPromo probe = new MallPromo();
        probe.setStatus(nextStatus);
        probe.setForever(nextForever);
        probe.setEndTime(nextEnd);
        assertSingleActive(exist.getStoreId(), exist.getId(), probe);

        record.setUpdateBy(operator.getUsername());
        mallPromoMapper.updateMallPromo(record);
        if (record.getTiers() != null) {
            List<MallPromoTier> tiers = validateTiers(record.getTiers());
            mallPromoTierMapper.deleteByPromoId(exist.getId());
            saveTiers(exist.getId(), tiers);
        }
        return Response.success();
    }

    @Override
    public Response deleteMallPromo(List<Long> idList, HttpServletRequest request) {
        Operator operator = storeAuthHelper.requireMerchantOrAdmin(request);
        if (ValidateUtil.isEmpty(idList)) {
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        List<Long> allowed = new ArrayList<>();
        for (Long id : idList) {
            if (id == null) {
                continue;
            }
            MallPromo exist = mallPromoMapper.selectPromoById(id);
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
        int affected = logicDeleteHelper.deleteByIds("mall_promo", allowed);
        return affected > 0 ? Response.success() : new Response(400, null, "操作失败，未找到需要删除的记录");
    }

    @Override
    public MallPromo findActiveByStoreId(Long storeId) {
        if (storeId == null) {
            return null;
        }
        MallPromo promo = mallPromoMapper.selectActiveByStoreId(storeId, new Date());
        if (promo == null) {
            return null;
        }
        promo.setTiers(mallPromoTierMapper.selectByPromoId(promo.getId()));
        return promo;
    }

    @Override
    public Response getActivePromo(Long storeId) {
        if (storeId == null) {
            throw new BusinessException("店铺不能为空");
        }
        MallPromo promo = findActiveByStoreId(storeId);
        if (promo == null) {
            return Response.success(null);
        }
        promo.setTierText(MallDiscountCalculator.tierText(promo.getTiers()));
        promo.setExpireText(MallDiscountCalculator.expireText(promo.getForever(), promo.getEndTime()));
        promo.setEffective(true);
        storeAuthHelper.fillStoreName(promo.getStoreId(), promo::setStoreName);
        return Response.success(promo);
    }

    private void saveTiers(Long promoId, List<MallPromoTier> tiers) {
        int sort = 0;
        List<MallPromoTier> rows = new ArrayList<>();
        for (MallPromoTier tier : tiers) {
            rows.add(MallPromoTier.builder()
                    .id(IdWorker.getId())
                    .promoId(promoId)
                    .thresholdAmount(tier.getThresholdAmount())
                    .discountAmount(tier.getDiscountAmount())
                    .sort(sort++)
                    .build());
        }
        mallPromoTierMapper.batchAdd(rows);
    }

    private List<MallPromoTier> validateTiers(List<MallPromoTier> raw) {
        if (ValidateUtil.isEmpty(raw)) {
            throw new BusinessException("请至少添加一档满减");
        }
        List<MallPromoTier> tiers = new ArrayList<>();
        Set<String> thresholds = new HashSet<>();
        for (MallPromoTier item : raw) {
            if (item == null) {
                continue;
            }
            BigDecimal threshold = money(item.getThresholdAmount(), "满减门槛");
            BigDecimal discount = money(item.getDiscountAmount(), "减免金额");
            if (threshold.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("满减门槛必须大于0");
            }
            if (discount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("减免金额必须大于0");
            }
            if (discount.compareTo(threshold) >= 0) {
                throw new BusinessException("减免金额必须小于满减门槛");
            }
            String key = threshold.stripTrailingZeros().toPlainString();
            if (!thresholds.add(key)) {
                throw new BusinessException("满减档位门槛不能重复");
            }
            tiers.add(MallPromoTier.builder()
                    .thresholdAmount(threshold)
                    .discountAmount(discount)
                    .build());
        }
        if (tiers.isEmpty()) {
            throw new BusinessException("请至少添加一档满减");
        }
        return tiers;
    }

    private void normalizeExpire(MallPromo record, boolean creating) {
        if (!StringUtils.hasText(record.getName())) {
            throw new BusinessException("请输入活动名称");
        }
        record.setName(record.getName().trim());
        if (record.getName().length() > 64) {
            throw new BusinessException("活动名称不能超过64个字");
        }
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

    private void assertSingleActive(Long storeId, Long excludeId, MallPromo next) {
        if (next.getStatus() == null || next.getStatus() != 1) {
            return;
        }
        if (!MallDiscountCalculator.isEffective(next.getForever(), next.getEndTime(), new Date())) {
            return;
        }
        int count = mallPromoMapper.countActiveByStoreId(storeId, excludeId, new Date());
        if (count > 0) {
            throw new BusinessException("该店铺已有进行中的满减，请先停用或等其过期");
        }
    }

    private void fillTiersAndDisplay(List<MallPromo> list) {
        if (ValidateUtil.isEmpty(list)) {
            return;
        }
        List<Long> ids = list.stream().map(MallPromo::getId).collect(Collectors.toList());
        List<MallPromoTier> allTiers = mallPromoTierMapper.selectByPromoIds(ids);
        Map<Long, List<MallPromoTier>> grouped = new HashMap<>();
        if (!ValidateUtil.isEmpty(allTiers)) {
            for (MallPromoTier tier : allTiers) {
                grouped.computeIfAbsent(tier.getPromoId(), key -> new ArrayList<>()).add(tier);
            }
        }
        Date now = new Date();
        for (MallPromo item : list) {
            List<MallPromoTier> tiers = grouped.getOrDefault(item.getId(), Collections.emptyList());
            item.setTiers(tiers);
            item.setTierText(MallDiscountCalculator.tierText(tiers));
            item.setExpireText(MallDiscountCalculator.expireText(item.getForever(), item.getEndTime()));
            boolean on = item.getStatus() != null && item.getStatus() == 1
                    && MallDiscountCalculator.isEffective(item.getForever(), item.getEndTime(), now);
            item.setEffective(on);
            storeAuthHelper.fillStoreName(item.getStoreId(), item::setStoreName);
        }
    }

    private BigDecimal money(BigDecimal value, String label) {
        if (value == null) {
            throw new BusinessException(label + "不能为空");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private Response buildPageResponse(List<MallPromo> list, Integer pageNum, Integer pageSize) {
        PageInfo<MallPromo> pageInfo = new PageInfo<>(list);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getList());
        data.put("total", pageInfo.getTotal());
        data.put("pages", pageInfo.getPages());
        data.put("pageNum", pageNum != null ? pageInfo.getPageNum() : 1);
        data.put("pageSize", pageSize != null ? pageInfo.getPageSize() : pageInfo.getTotal());
        return new Response(200, data, "操作成功");
    }
}
