package com.example.springboottemplate.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.mall.MallPayPrepareVO;
import com.example.springboottemplate.dto.mall.StoreMemberAssistRechargeRequest;
import com.example.springboottemplate.dto.mall.StoreMemberCustomerVO;
import com.example.springboottemplate.dto.mall.StoreMemberRechargeRequest;
import com.example.springboottemplate.dto.mall.StoreMemberStatusRequest;
import com.example.springboottemplate.dto.mall.StoreMemberVerifyDebitRequest;
import com.example.springboottemplate.dto.mall.StoreMemberVO;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.MerchantMember;
import com.example.springboottemplate.entity.StoreMember;
import com.example.springboottemplate.entity.StoreMemberRecharge;
import com.example.springboottemplate.entity.StoreVerifyLog;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.mapper.StoreMemberMapper;
import com.example.springboottemplate.mapper.StoreMemberRechargeMapper;
import com.example.springboottemplate.mapper.StoreVerifyLogMapper;
import com.example.springboottemplate.mapper.system.UserMapper;
import com.example.springboottemplate.service.StoreMemberService;
import com.example.springboottemplate.service.wx.WxPayClientService;
import com.example.springboottemplate.service.wx.WxShippingService;
import com.example.springboottemplate.service.wx.WxTenantContext;
import com.example.springboottemplate.service.wx.WxTenantResolver;
import com.example.springboottemplate.utils.MiniappUiHelper;
import com.example.springboottemplate.utils.StoreAuthHelper;
import com.example.springboottemplate.utils.StoreExpireHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
    public static final BigDecimal MIN_ASSIST_RECHARGE = new BigDecimal("0.01");
    public static final BigDecimal MAX_RECHARGE = new BigDecimal("100000");

    @Autowired
    private StoreMemberMapper storeMemberMapper;
    @Autowired
    private com.example.springboottemplate.mapper.MerchantMemberMapper merchantMemberMapper;
    @Autowired
    private StoreMemberRechargeMapper storeMemberRechargeMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StoreVerifyLogMapper storeVerifyLogMapper;
    @Autowired
    private StoreAuthHelper storeAuthHelper;
    @Autowired
    private WxPayClientService wxPayClientService;
    @Autowired
    private WxTenantResolver wxTenantResolver;
    @Autowired
    private WxShippingService wxShippingService;

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
        if (amount.compareTo(MAX_RECHARGE) > 0) {
            throw new BusinessException("储值金额过大");
        }
        Long storeId = resolveBrowseStoreId(request.getStoreId(), user);
        if (storeId == null) {
            throw new BusinessException("请选择店铺");
        }
        assertStoreCanVerify(storeId);
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
        touchStoreMember(storeId, customer.getId(), now);
        Long merchantUserId = storeAuthHelper.findMerchantUserId(storeId);
        if (merchantUserId != null) {
            ensureMerchantMember(merchantUserId, customer.getId(), now);
            merchantMemberMapper.updateMemberStatus(merchantUserId, customer.getId(), request.getIsMember(), now);
        } else if (member == null) {
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
    public Response lookupByPhone(String phone, Long storeId, HttpServletRequest httpRequest) {
        return lookupCustomer(phone, null, storeId, httpRequest);
    }

    @Override
    public Response lookupCustomer(String phone, Long userId, Long storeId, HttpServletRequest httpRequest) {
        StoreAuthHelper.Operator operator = storeAuthHelper.requireVerifyOperator(httpRequest);
        Long targetStoreId = storeAuthHelper.resolveVerifyStoreId(operator, storeId);
        // 查询顾客只需要店铺接入支付；储值/核验的额外限制在各自接口校验
        assertStoreCanPay(targetStoreId);
        if (userId == null && !StringUtils.hasText(phone)) {
            throw new BusinessException("请选择顾客");
        }
        User customer = userId != null
                ? findOrdinaryCustomerByUserId(userId)
                : findOrdinaryCustomerByPhone(targetStoreId, phone);
        return Response.success(toCustomerVo(targetStoreId, customer));
    }

    @Override
    public Response assistRecharge(StoreMemberAssistRechargeRequest request, HttpServletRequest httpRequest) {
        StoreAuthHelper.Operator operator = storeAuthHelper.requireMerchantOrAdmin(httpRequest);
        if (request == null || (request.getUserId() == null && !StringUtils.hasText(request.getPhone()))) {
            throw new BusinessException("请选择顾客");
        }
        if (request.getAmount() == null) {
            throw new BusinessException("请输入储值金额");
        }
        BigDecimal amount = request.getAmount().setScale(2, RoundingMode.HALF_UP);
        if (amount.compareTo(MIN_ASSIST_RECHARGE) < 0) {
            throw new BusinessException("储值金额至少0.01元");
        }
        if (amount.compareTo(MAX_RECHARGE) > 0) {
            throw new BusinessException("储值金额过大");
        }
        Long storeId = storeAuthHelper.resolveWritableStoreId(operator, request.getStoreId());
        assertStoreCanVerify(storeId);
        User customer = request.getUserId() != null
                ? findOrdinaryCustomerByUserId(request.getUserId())
                : findOrdinaryCustomerByPhone(storeId, request.getPhone());
        Date now = new Date();
        creditAndJoin(storeId, customer.getId(), amount, now);

        int amountFen = amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValueExact();
        String rechargeNo = generateRechargeNo("M");
        WxTenantContext payTenant = wxTenantResolver.byStoreIdForNotify(storeId);
        String remark = clipRemark(request.getRemark());
        StoreMemberRecharge recharge = StoreMemberRecharge.builder()
                .id(IdWorker.getId())
                .rechargeNo(rechargeNo)
                .storeId(storeId)
                .userId(customer.getId())
                .openid(customer.getOpenid())
                .appId(payTenant == null ? null : payTenant.getAppId())
                .mchId(payTenant == null ? null : payTenant.getMchId())
                .amount(amount)
                .amountFen(amountFen)
                .remark(remark)
                .payStatus(1)
                .transactionId("MCH" + rechargeNo)
                .paidTime(now)
                .wxShippingStatus(1)
                .createdBy(operator.getUsername())
                .createdTime(now)
                .deleted(0)
                .build();
        storeMemberRechargeMapper.addRecharge(recharge);

        StoreMember latest = find(storeId, customer.getId());
        return Response.success(toCustomerVo(storeId, customer, latest));
    }

    @Override
    public Response verifyDebit(StoreMemberVerifyDebitRequest request, HttpServletRequest httpRequest) {
        StoreAuthHelper.Operator operator = storeAuthHelper.requireVerifyOperator(httpRequest);
        if (request == null) {
            throw new BusinessException("请填写核验信息");
        }
        Long storeId = storeAuthHelper.resolveVerifyStoreId(operator, request.getStoreId());
        assertStoreCanVerify(storeId);
        User customer = findOrdinaryCustomerByPhone(storeId, request.getPhone());
        BigDecimal amount = request.getAmount() == null
                ? null
                : request.getAmount().setScale(2, RoundingMode.HALF_UP);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("请输入正确的核验金额");
        }
        StoreMember member = find(storeId, customer.getId());
        BigDecimal balance = member == null || member.getBalance() == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : member.getBalance().setScale(2, RoundingMode.HALF_UP);
        if (member == null || !Integer.valueOf(1).equals(member.getIsMember())
                || balance.compareTo(amount) < 0) {
            throw new BusinessException("余额不足");
        }
        if (!debit(storeId, customer.getId(), amount)) {
            throw new BusinessException("余额不足");
        }
        User operatorUser = userMapper.selectById(operator.getUserId());
        storeVerifyLogMapper.addStoreVerifyLog(StoreVerifyLog.builder()
                .id(IdWorker.getId())
                .storeId(storeId)
                .customerUserId(customer.getId())
                .customerPhone(customer.getPhone())
                .customerName(resolveOperatorName(customer))
                .amount(amount)
                .remark(clipRemark(request.getRemark()))
                .operatorUserId(operator.getUserId())
                .operatorType(resolveOperatorType(operator))
                .operatorName(resolveOperatorName(operatorUser, operator.getUsername()))
                .createdBy(operator.getUsername())
                .createdTime(new Date())
                .build());
        StoreMember latest = find(storeId, customer.getId());
        return Response.success(toCustomerVo(storeId, customer, latest));
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
        scheduleWxShippingAfterCommit(recharge.getRechargeNo());
    }

    /** 事务提交后再报虚拟发货，避免挡住支付回调、也避免未提交充值单被微信查不到 */
    private void scheduleWxShippingAfterCommit(String rechargeNo) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            wxShippingService.uploadRechargeShippingAsync(rechargeNo);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                wxShippingService.uploadRechargeShippingAsync(rechargeNo);
            }
        });
    }

    @Override
    public StoreMember find(Long storeId, Long userId) {
        if (storeId == null || userId == null) {
            return null;
        }
        Long merchantUserId = storeAuthHelper.findMerchantUserId(storeId);
        if (merchantUserId != null) {
            MerchantMember chain = merchantMemberMapper.selectByMerchantAndUser(merchantUserId, userId);
            if (chain == null) {
                return storeMemberMapper.selectByStoreAndUser(storeId, userId);
            }
            return StoreMember.builder()
                    .storeId(storeId)
                    .userId(userId)
                    .isMember(chain.getIsMember())
                    .balance(chain.getBalance())
                    .build();
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
        Date now = new Date();
        BigDecimal money = amount.setScale(2, RoundingMode.HALF_UP);
        Long merchantUserId = storeAuthHelper.findMerchantUserId(storeId);
        boolean ok;
        if (merchantUserId != null) {
            ok = merchantMemberMapper.debitBalance(merchantUserId, userId, money, now) > 0;
        } else {
            ok = storeMemberMapper.debitBalance(storeId, userId, money, now) > 0;
        }
        if (ok) {
            touchStoreMember(storeId, userId, now);
        }
        return ok;
    }

    @Override
    public void credit(Long storeId, Long userId, BigDecimal amount) {
        if (storeId == null || userId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        Date now = new Date();
        BigDecimal money = amount.setScale(2, RoundingMode.HALF_UP);
        Long merchantUserId = storeAuthHelper.findMerchantUserId(storeId);
        if (merchantUserId != null) {
            int rows = merchantMemberMapper.creditBalance(merchantUserId, userId, money, now);
            if (rows <= 0) {
                creditChainAndJoin(merchantUserId, userId, money, now);
            }
            touchStoreMember(storeId, userId, now);
            return;
        }
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
    public int markStoreSettled(Long storeId, Date settleTime, String settleBy, String platformMchId) {
        if (storeId == null) {
            return 0;
        }
        return storeMemberRechargeMapper.markStoreSettled(storeId, settleTime, settleBy, platformMchId);
    }

    private void creditAndJoin(Long storeId, Long userId, BigDecimal amount, Date now) {
        if (storeId == null || userId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal money = amount.setScale(2, RoundingMode.HALF_UP);
        Long merchantUserId = storeAuthHelper.findMerchantUserId(storeId);
        if (merchantUserId != null) {
            creditChainAndJoin(merchantUserId, userId, money, now);
            touchStoreMember(storeId, userId, now);
            return;
        }
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

    private void creditChainAndJoin(Long merchantUserId, Long userId, BigDecimal money, Date now) {
        int rows = merchantMemberMapper.addBalanceAndJoin(merchantUserId, userId, money, now);
        if (rows > 0) {
            return;
        }
        try {
            merchantMemberMapper.insertMember(MerchantMember.builder()
                    .id(IdWorker.getId())
                    .merchantUserId(merchantUserId)
                    .userId(userId)
                    .isMember(1)
                    .balance(money)
                    .createdTime(now)
                    .updateTime(now)
                    .deleted(0)
                    .build());
        } catch (Exception e) {
            merchantMemberMapper.addBalanceAndJoin(merchantUserId, userId, money, now);
        }
    }

    private void ensureMerchantMember(Long merchantUserId, Long userId, Date now) {
        if (merchantMemberMapper.selectByMerchantAndUser(merchantUserId, userId) != null) {
            return;
        }
        try {
            merchantMemberMapper.insertMember(MerchantMember.builder()
                    .id(IdWorker.getId())
                    .merchantUserId(merchantUserId)
                    .userId(userId)
                    .isMember(0)
                    .balance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .createdTime(now)
                    .updateTime(now)
                    .deleted(0)
                    .build());
        } catch (Exception ignored) {
            // 并发插入
        }
    }

    @Override
    public void touchCustomer(Long storeId, Long userId) {
        touchStoreMember(storeId, userId, new Date());
    }

    private void touchStoreMember(Long storeId, Long userId, Date now) {
        if (storeId == null || userId == null) {
            return;
        }
        if (storeMemberMapper.selectByStoreAndUser(storeId, userId) != null) {
            return;
        }
        try {
            storeMemberMapper.insertMember(StoreMember.builder()
                    .id(IdWorker.getId())
                    .storeId(storeId)
                    .userId(userId)
                    .isMember(0)
                    .balance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .createdTime(now)
                    .updateTime(now)
                    .deleted(0)
                    .build());
        } catch (Exception ignored) {
            // 并发插入
        }
    }

    private Long resolveBrowseStoreId(Long requestedStoreId, User user) {
        if (requestedStoreId != null) {
            return requestedStoreId;
        }
        return user == null ? null : user.getBindStoreId();
    }

    private String generateRechargeNo() {
        return generateRechargeNo("S");
    }

    private String generateRechargeNo(String prefix) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rnd = ThreadLocalRandom.current().nextInt(100000, 999999);
        return (StringUtils.hasText(prefix) ? prefix : "S") + time + rnd;
    }

    private String clipRemark(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String remark = raw.trim();
        return remark.length() > 200 ? remark.substring(0, 200) : remark;
    }

    /** 店铺接入支付校验：查询顾客等基础能力只需要这一层 */
    private void assertStoreCanPay(Long storeId) {
        Store store = storeMapper.selectByStoreId(storeId);
        if (store == null) {
            throw new BusinessException("店铺不存在");
        }
        if (StoreExpireHelper.isExpired(store)) {
            throw new BusinessException(StoreExpireHelper.EXPIRED_BROWSE_MSG);
        }
        MiniappUiHelper.fillDefaults(store);
        if (!MiniappUiHelper.enablePay(store)) {
            throw new BusinessException("当前店铺未接入支付");
        }
    }

    /** 储值/核验校验：在支付之上还要求店铺接入储值 */
    private void assertStoreCanVerify(Long storeId) {
        assertStoreCanPay(storeId);
        Store store = storeMapper.selectByStoreId(storeId);
        MiniappUiHelper.fillDefaults(store);
        if (!MiniappUiHelper.enableRecharge(store)) {
            throw new BusinessException("当前店铺未接入储值");
        }
    }

    private int resolveOperatorType(StoreAuthHelper.Operator operator) {
        if (operator.isAdmin()) {
            return 3;
        }
        if (operator.isStaff()) {
            return 2;
        }
        return 1;
    }

    private String resolveOperatorName(User user) {
        return resolveOperatorName(user, null);
    }

    private String resolveOperatorName(User user, String fallback) {
        if (user != null && StringUtils.hasText(user.getRealName())) {
            return user.getRealName().trim();
        }
        if (user != null && StringUtils.hasText(user.getUserName())) {
            return user.getUserName().trim();
        }
        if (user != null && StringUtils.hasText(user.getPhone())) {
            return user.getPhone().trim();
        }
        return StringUtils.hasText(fallback) ? fallback.trim() : "未知";
    }

    private User findOrdinaryCustomerByPhone(Long storeId, String rawPhone) {
        String phone = rawPhone == null ? "" : rawPhone.trim();
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("请输入正确的手机号");
        }
        User customer = userMapper.selectByPhoneAndBindStore(phone, storeId);
        if (customer == null) {
            customer = userMapper.selectByPhone(phone);
        }
        if (customer == null || (customer.getDeleted() != null && customer.getDeleted() == 1)) {
            throw new BusinessException("未找到该手机号对应的顾客");
        }
        if (customer.getIdentityType() == null || customer.getIdentityType() != 1) {
            throw new BusinessException("只能操作普通顾客");
        }
        return customer;
    }

    private User findOrdinaryCustomerByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException("请选择顾客");
        }
        User customer = userMapper.selectById(userId);
        if (customer == null || (customer.getDeleted() != null && customer.getDeleted() == 1)) {
            throw new BusinessException("顾客不存在");
        }
        if (customer.getIdentityType() == null || customer.getIdentityType() != 1) {
            throw new BusinessException("只能给普通顾客储值");
        }
        return customer;
    }

    private StoreMemberCustomerVO toCustomerVo(Long storeId, User customer) {
        return toCustomerVo(storeId, customer, find(storeId, customer.getId()));
    }

    private StoreMemberCustomerVO toCustomerVo(Long storeId, User customer, StoreMember member) {
        boolean isMember = member != null && Integer.valueOf(1).equals(member.getIsMember());
        BigDecimal balance = member == null || member.getBalance() == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : member.getBalance().setScale(2, RoundingMode.HALF_UP);
        String nickname = customer.getRealName();
        if (!StringUtils.hasText(nickname)) {
            nickname = "微信用户";
        }
        return StoreMemberCustomerVO.builder()
                .userId(customer.getId())
                .storeId(storeId)
                .phone(customer.getPhone())
                .nickname(nickname.trim())
                .member(isMember)
                .balance(balance)
                .build();
    }
}
