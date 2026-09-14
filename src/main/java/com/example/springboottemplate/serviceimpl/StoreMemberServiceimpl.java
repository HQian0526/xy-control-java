package com.example.springboottemplate.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.mall.MallPayPrepareVO;
import com.example.springboottemplate.dto.mall.StoreMemberRechargeRequest;
import com.example.springboottemplate.dto.mall.StoreMemberStatusRequest;
import com.example.springboottemplate.dto.mall.StoreMemberVO;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.StoreMember;
import com.example.springboottemplate.entity.StoreMemberRecharge;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.mapper.StoreMemberMapper;
import com.example.springboottemplate.mapper.StoreMemberRechargeMapper;
import com.example.springboottemplate.mapper.system.UserMapper;
import com.example.springboottemplate.service.StoreMemberService;
import com.example.springboottemplate.service.wx.WxPayClientService;
import com.example.springboottemplate.service.wx.WxTenantContext;
import com.example.springboottemplate.service.wx.WxTenantResolver;
import com.example.springboottemplate.utils.StoreAuthHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class StoreMemberServiceimpl implements StoreMemberService {

    public static final BigDecimal MIN_RECHARGE = new BigDecimal("100.00");

    @Autowired
    private StoreMemberMapper storeMemberMapper;
    @Autowired
    private StoreMemberRechargeMapper storeMemberRechargeMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StoreAuthHelper storeAuthHelper;
    @Autowired
    private WxPayClientService wxPayClientService;
    @Autowired
    private WxTenantResolver wxTenantResolver;

    @Override
    public Response mine(Long storeId, HttpServletRequest request) {
        User user = storeAuthHelper.requireLoginUser(request);
        Long targetStoreId = resolveBrowseStoreId(storeId, user);
        StoreMember member = targetStoreId == null ? null : find(targetStoreId, user.getId());
        boolean isMember = member != null && Integer.valueOf(1).equals(member.getIsMember());
        BigDecimal balance = member == null || member.getBalance() == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : member.getBalance().setScale(2, RoundingMode.HALF_UP);
        return Response.success(StoreMemberVO.builder()
                .storeId(targetStoreId)
                .member(isMember)
                .balance(balance)
                .build());
    }

    @Override
    public Response recharge(StoreMemberRechargeRequest request, HttpServletRequest httpRequest) {
        User user = storeAuthHelper.requireLoginUser(httpRequest);
        if (request == null || request.getAmount() == null) {
            throw new BusinessException("请输入储值金额");
        }
        BigDecimal amount = request.getAmount().setScale(2, RoundingMode.HALF_UP);
        if (amount.compareTo(MIN_RECHARGE) < 0) {
            throw new BusinessException("每次储值至少100元");
        }
        if (amount.compareTo(new BigDecimal("100000")) > 0) {
            throw new BusinessException("储值金额过大");
        }
        Long storeId = resolveBrowseStoreId(request.getStoreId(), user);
        if (storeId == null) {
            throw new BusinessException("请选择店铺");
        }
        Store store = storeMapper.selectByStoreId(storeId);
        if (store == null || (store.getDeleted() != null && store.getDeleted() == 1)) {
            throw new BusinessException("未找到店铺信息");
        }
        if (!StringUtils.hasText(user.getOpenid()) && !wxPayClientService.isMock()) {
            throw new BusinessException("当前账号未绑定微信，请先微信登录");
        }

        WxTenantContext payTenant = wxTenantResolver.byStoreId(storeId);
        if (!wxPayClientService.isMock()) {
            String userAppId = StringUtils.hasText(user.getAppId())
                    ? user.getAppId()
                    : wxTenantResolver.platform().getAppId();
            if (StringUtils.hasText(payTenant.getAppId())
                    && StringUtils.hasText(userAppId)
                    && !payTenant.getAppId().equals(userAppId)) {
                throw new BusinessException("请使用该店小程序储值");
            }
        }

        int amountFen = amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValueExact();
        Date now = new Date();
        String rechargeNo = generateRechargeNo();
        StoreMemberRecharge recharge = StoreMemberRecharge.builder()
                .id(IdWorker.getId())
                .rechargeNo(rechargeNo)
                .storeId(storeId)
                .userId(user.getId())
                .openid(user.getOpenid())
                .appId(payTenant.getAppId())
                .mchId(payTenant.getMchId())
                .amount(amount)
                .amountFen(amountFen)
                .payStatus(0)
                .createdBy(user.getUserName())
                .createdTime(now)
                .deleted(0)
                .build();
        storeMemberRechargeMapper.addRecharge(recharge);

        MallPayPrepareVO payVO = wxPayClientService.createJsapiPrepay(
                payTenant, rechargeNo, "会员储值", user.getOpenid(), amountFen);
        if (!payVO.isMock() && StringUtils.hasText(payVO.getPackageValue())
                && payVO.getPackageValue().startsWith("prepay_id=")) {
            String prepayId = payVO.getPackageValue().substring("prepay_id=".length());
            storeMemberRechargeMapper.updatePrepayId(rechargeNo, prepayId);
        }
        payVO.setOrderNo(rechargeNo);
        payVO.setPayAmount(amount);
        payVO.setPayStatus(0);
        return Response.success(payVO);
    }

    @Override
    public Response queryRecharge(String rechargeNo, HttpServletRequest httpRequest) {
        User user = storeAuthHelper.requireLoginUser(httpRequest);
        if (!StringUtils.hasText(rechargeNo)) {
            throw new BusinessException("充值单号不能为空");
        }
        StoreMemberRecharge recharge = storeMemberRechargeMapper.selectByRechargeNo(rechargeNo.trim());
        if (recharge == null) {
            throw new BusinessException("充值单不存在");
        }
        if (recharge.getUserId() == null || !recharge.getUserId().equals(user.getId())) {
            Integer identityType = user.getIdentityType();
            if (identityType == null || identityType != 3) {
                throw new BusinessException("无权查看该充值单");
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("recharge", recharge);
        data.put("mock", wxPayClientService.isMock());
        return Response.success(data);
    }

    @Override
    public Response mockConfirmRecharge(String rechargeNo, HttpServletRequest httpRequest) {
        if (!wxPayClientService.isMock()) {
            throw new BusinessException("仅 mock 模式可用");
        }
        User user = storeAuthHelper.requireLoginUser(httpRequest);
        if (!StringUtils.hasText(rechargeNo)) {
            throw new BusinessException("充值单号不能为空");
        }
        StoreMemberRecharge recharge = storeMemberRechargeMapper.selectByRechargeNo(rechargeNo.trim());
        if (recharge == null) {
            throw new BusinessException("充值单不存在");
        }
        if (recharge.getUserId() == null || !recharge.getUserId().equals(user.getId())) {
            throw new BusinessException("无权操作该充值单");
        }
        if (recharge.getPayStatus() != null && recharge.getPayStatus() == 1) {
            return Response.success(recharge);
        }
        if (recharge.getPayStatus() != null && recharge.getPayStatus() != 0) {
            throw new BusinessException("充值单状态不允许支付");
        }
        applyRechargePaid(recharge.getRechargeNo(), "MOCK" + recharge.getRechargeNo());
        return Response.success(storeMemberRechargeMapper.selectByRechargeNo(recharge.getRechargeNo()));
    }

    @Override
    public Response updateStatus(StoreMemberStatusRequest request, HttpServletRequest httpRequest) {
        StoreAuthHelper.Operator operator = storeAuthHelper.requireMerchantOrAdmin(httpRequest);
        if (request == null || request.getUserId() == null) {
            throw new BusinessException("请选择顾客");
        }
        if (request.getIsMember() == null || (request.getIsMember() != 0 && request.getIsMember() != 1)) {
            throw new BusinessException("身份仅支持基础用户或会员");
        }
        Long storeId = storeAuthHelper.resolveWritableStoreId(operator, request.getStoreId());
        User customer = userMapper.selectById(request.getUserId());
        if (customer == null || (customer.getDeleted() != null && customer.getDeleted() == 1)) {
            throw new BusinessException("顾客不存在");
        }
        if (customer.getIdentityType() == null || customer.getIdentityType() != 1) {
            throw new BusinessException("只能修改普通顾客的会员身份");
        }
        StoreMember member = find(storeId, customer.getId());
        Date now = new Date();
        if (request.getIsMember() == 0) {
            BigDecimal balance = member == null || member.getBalance() == null ? BigDecimal.ZERO : member.getBalance();
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                throw new BusinessException("该顾客仍有会员余额，无法降为基础用户");
            }
        }
        if (member == null) {
            storeMemberMapper.insertMember(StoreMember.builder()
                    .id(IdWorker.getId())
                    .storeId(storeId)
                    .userId(customer.getId())
                    .isMember(request.getIsMember())
                    .balance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .createdTime(now)
                    .updateTime(now)
                    .deleted(0)
                    .build());
        } else {
            storeMemberMapper.updateMemberStatus(storeId, customer.getId(), request.getIsMember(), now);
        }
        StoreMember latest = find(storeId, customer.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("userId", String.valueOf(customer.getId()));
        data.put("storeId", String.valueOf(storeId));
        data.put("isMember", latest == null ? 0 : latest.getIsMember());
        data.put("balance", latest == null || latest.getBalance() == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : latest.getBalance().setScale(2, RoundingMode.HALF_UP));
        return Response.success(data);
    }

    @Override
    public void applyRechargePaid(String rechargeNo, String transactionId) {
        if (!StringUtils.hasText(rechargeNo)) {
            return;
        }
        StoreMemberRecharge recharge = storeMemberRechargeMapper.selectByRechargeNo(rechargeNo.trim());
        if (recharge == null) {
            return;
        }
        if (recharge.getPayStatus() != null && recharge.getPayStatus() == 1) {
            return;
        }
        Date paidTime = new Date();
        int rows = storeMemberRechargeMapper.markPaid(rechargeNo.trim(), transactionId, paidTime);
        if (rows <= 0) {
            return;
        }
        creditAndJoin(recharge.getStoreId(), recharge.getUserId(), recharge.getAmount(), paidTime);
    }

    @Override
    public StoreMember find(Long storeId, Long userId) {
        if (storeId == null || userId == null) {
            return null;
        }
        return storeMemberMapper.selectByStoreAndUser(storeId, userId);
    }

    @Override
    public boolean debit(Long storeId, Long userId, BigDecimal amount) {
        if (storeId == null || userId == null || amount == null) {
            return false;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }
        return storeMemberMapper.debitBalance(storeId, userId, amount.setScale(2, RoundingMode.HALF_UP), new Date()) > 0;
    }

    @Override
    public void credit(Long storeId, Long userId, BigDecimal amount) {
        if (storeId == null || userId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        Date now = new Date();
        BigDecimal money = amount.setScale(2, RoundingMode.HALF_UP);
        int rows = storeMemberMapper.creditBalance(storeId, userId, money, now);
        if (rows <= 0) {
            creditAndJoin(storeId, userId, money, now);
        }
    }

    @Override
    public int closeExpiredUnpaid(Date expireBefore, int limit) {
        if (expireBefore == null || limit <= 0) {
            return 0;
        }
        List<StoreMemberRecharge> pending = storeMemberRechargeMapper.selectExpiredUnpaid(expireBefore, limit);
        if (pending == null || pending.isEmpty()) {
            return 0;
        }
        int closed = 0;
        Date now = new Date();
        for (StoreMemberRecharge recharge : pending) {
            if (recharge == null || !StringUtils.hasText(recharge.getRechargeNo())) {
                continue;
            }
            closed += storeMemberRechargeMapper.markClosed(recharge.getRechargeNo(), now);
        }
        return closed;
    }

    @Override
    public int markStoreSettled(Long storeId, Date settleTime, String settleBy) {
        if (storeId == null) {
            return 0;
        }
        return storeMemberRechargeMapper.markStoreSettled(storeId, settleTime, settleBy);
    }

    private void creditAndJoin(Long storeId, Long userId, BigDecimal amount, Date now) {
        if (storeId == null || userId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal money = amount.setScale(2, RoundingMode.HALF_UP);
        int rows = storeMemberMapper.addBalanceAndJoin(storeId, userId, money, now);
        if (rows > 0) {
            return;
        }
        try {
            storeMemberMapper.insertMember(StoreMember.builder()
                    .id(IdWorker.getId())
                    .storeId(storeId)
                    .userId(userId)
                    .isMember(1)
                    .balance(money)
                    .createdTime(now)
                    .updateTime(now)
                    .deleted(0)
                    .build());
        } catch (Exception e) {
            storeMemberMapper.addBalanceAndJoin(storeId, userId, money, now);
        }
    }

    private Long resolveBrowseStoreId(Long requestedStoreId, User user) {
        if (requestedStoreId != null) {
            return requestedStoreId;
        }
        return user == null ? null : user.getBindStoreId();
    }

    private String generateRechargeNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rnd = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "S" + time + rnd;
    }
}
