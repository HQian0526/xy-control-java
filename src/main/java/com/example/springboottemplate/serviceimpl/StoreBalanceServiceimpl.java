package com.example.springboottemplate.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.mall.MallPayPrepareVO;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.StoreBalanceAdjust;
import com.example.springboottemplate.entity.StoreBalanceRecharge;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.StoreBalanceAdjustMapper;
import com.example.springboottemplate.mapper.StoreBalanceRechargeMapper;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.service.StoreBalanceService;
import com.example.springboottemplate.service.wx.WxPayClientService;
import com.example.springboottemplate.service.wx.WxShippingService;
import com.example.springboottemplate.service.wx.WxTenantContext;
import com.example.springboottemplate.service.wx.WxTenantResolver;
import com.example.springboottemplate.utils.StoreAuthHelper;
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
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class StoreBalanceServiceimpl implements StoreBalanceService {

    public static final BigDecimal MIN_AMOUNT = new BigDecimal("1.00");
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("100000");

    @Autowired
    private StoreBalanceRechargeMapper storeBalanceRechargeMapper;
    @Autowired
    private StoreBalanceAdjustMapper storeBalanceAdjustMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private StoreAuthHelper storeAuthHelper;
    @Autowired
    private WxPayClientService wxPayClientService;
    @Autowired
    private WxTenantResolver wxTenantResolver;
    @Autowired
    private WxShippingService wxShippingService;

    @Override
    public Response recharge(Long storeId, BigDecimal amount, HttpServletRequest request) {
        StoreAuthHelper.Operator operator = storeAuthHelper.requireMerchantOrAdmin(request);
        if (!operator.isMerchant()) {
            throw new BusinessException("仅店长可为店铺储值");
        }
        if (amount == null) {
            throw new BusinessException("请输入储值金额");
        }
        BigDecimal money = amount.setScale(2, RoundingMode.HALF_UP);
        if (money.compareTo(MIN_AMOUNT) < 0) {
            throw new BusinessException("每次储值至少1元");
        }
        if (money.compareTo(MAX_AMOUNT) > 0) {
            throw new BusinessException("储值金额过大");
        }
        Long targetStoreId = storeAuthHelper.resolveWritableStoreId(operator, storeId);
        Store store = storeMapper.selectByStoreId(targetStoreId);
        if (store == null || (store.getDeleted() != null && store.getDeleted() == 1)) {
            throw new BusinessException("未找到店铺信息");
        }
        if (wxTenantResolver.storeUsesOwnMch(store.getStoreId())) {
            throw new BusinessException("店铺已接入商户号，收款直接进入商家账户，无需储值");
        }
        User user = storeAuthHelper.requireLoginUser(request);
        if (!StringUtils.hasText(user.getOpenid()) && !wxPayClientService.isMock()) {
            throw new BusinessException("当前账号未绑定微信，请先微信登录");
        }
        WxTenantContext payTenant = wxTenantResolver.platform();
        if (!wxPayClientService.isMock()) {
            String userAppId = StringUtils.hasText(user.getAppId()) ? user.getAppId() : payTenant.getAppId();
            if (StringUtils.hasText(payTenant.getAppId())
                    && StringUtils.hasText(userAppId)
                    && !payTenant.getAppId().equals(userAppId)) {
                throw new BusinessException("请使用平台小程序为店铺储值");
            }
        }

        int amountFen = money.movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValueExact();
        Date now = new Date();
        String rechargeNo = generateRechargeNo();
        StoreBalanceRecharge recharge = StoreBalanceRecharge.builder()
                .id(IdWorker.getId())
                .rechargeNo(rechargeNo)
                .storeId(targetStoreId)
                .userId(user.getId())
                .openid(user.getOpenid())
                .appId(payTenant.getAppId())
                .mchId(payTenant.getMchId())
                .amount(money)
                .amountFen(amountFen)
                .payStatus(0)
                .wxShippingStatus(0)
                .wxShippingRetry(0)
                .settleStatus(0)
                .createdBy(user.getUserName())
                .createdTime(now)
                .deleted(0)
                .build();
        storeBalanceRechargeMapper.addRecharge(recharge);

        MallPayPrepareVO payVO = wxPayClientService.createJsapiPrepay(
                payTenant, rechargeNo, "店铺余额储值", user.getOpenid(), amountFen);
        if (!payVO.isMock() && StringUtils.hasText(payVO.getPackageValue())
                && payVO.getPackageValue().startsWith("prepay_id=")) {
            storeBalanceRechargeMapper.updatePrepayId(
                    rechargeNo, payVO.getPackageValue().substring("prepay_id=".length()));
        }
        payVO.setOrderNo(rechargeNo);
        payVO.setPayAmount(money);
        payVO.setPayStatus(0);
        return Response.success(payVO);
    }

    @Override
    public Response query(String rechargeNo, HttpServletRequest request) {
        User user = storeAuthHelper.requireLoginUser(request);
        StoreBalanceRecharge recharge = requireOwn(rechargeNo, user);
        Map<String, Object> data = new HashMap<>();
        data.put("recharge", recharge);
        data.put("mock", wxPayClientService.isMock());
        return Response.success(data);
    }

    @Override
    public Response mockConfirm(String rechargeNo, HttpServletRequest request) {
        if (!wxPayClientService.isMock()) {
            throw new BusinessException("仅 mock 模式可用");
        }
        User user = storeAuthHelper.requireLoginUser(request);
        StoreBalanceRecharge recharge = requireOwn(rechargeNo, user);
        if (recharge.getPayStatus() != null && recharge.getPayStatus() == 1) {
            return Response.success(recharge);
        }
        if (recharge.getPayStatus() != null && recharge.getPayStatus() != 0) {
            throw new BusinessException("储值单状态不允许支付");
        }
        applyPaid(recharge.getRechargeNo(), "MOCK" + recharge.getRechargeNo());
        return Response.success(storeBalanceRechargeMapper.selectByRechargeNo(recharge.getRechargeNo()));
    }

    @Override
    public void applyPaid(String rechargeNo, String transactionId) {
        if (!StringUtils.hasText(rechargeNo)) {
            return;
        }
        StoreBalanceRecharge recharge = storeBalanceRechargeMapper.selectByRechargeNo(rechargeNo.trim());
        if (recharge == null || (recharge.getPayStatus() != null && recharge.getPayStatus() == 1)) {
            return;
        }
        Date paidTime = new Date();
        int rows = storeBalanceRechargeMapper.markPaid(rechargeNo.trim(), transactionId, paidTime);
        if (rows <= 0) {
            return;
        }
        scheduleShipping(rechargeNo.trim());
    }

    @Override
    public int markStoreSettled(Long storeId, Date settleTime, String settleBy) {
        if (storeId == null) {
            return 0;
        }
        return storeBalanceRechargeMapper.markStoreSettled(storeId, settleTime, settleBy);
    }

    @Override
    public void addSettledRefundClawback(Long storeId, String outRefundNo, String orderNo, int refundFen) {
        if (storeId == null || refundFen <= 0 || !StringUtils.hasText(outRefundNo)) {
            return;
        }
        BigDecimal amount = BigDecimal.valueOf(refundFen).movePointLeft(2).negate();
        storeBalanceAdjustMapper.insertIgnore(StoreBalanceAdjust.builder()
                .id(IdWorker.getId())
                .storeId(storeId)
                .amount(amount)
                .bizNo(outRefundNo.trim())
                .orderNo(orderNo)
                .remark("已结算订单退款冲减")
                .settleStatus(0)
                .deleted(0)
                .createdTime(new Date())
                .build());
    }

    @Override
    public int markAdjustSettled(Long storeId, Date settleTime, String settleBy) {
        if (storeId == null) {
            return 0;
        }
        return storeBalanceAdjustMapper.markStoreSettled(storeId, settleTime, settleBy);
    }

    private StoreBalanceRecharge requireOwn(String rechargeNo, User user) {
        if (!StringUtils.hasText(rechargeNo)) {
            throw new BusinessException("储值单号不能为空");
        }
        StoreBalanceRecharge recharge = storeBalanceRechargeMapper.selectByRechargeNo(rechargeNo.trim());
        if (recharge == null) {
            throw new BusinessException("储值单不存在");
        }
        if (recharge.getUserId() == null || !recharge.getUserId().equals(user.getId())) {
            Integer identityType = user.getIdentityType();
            if (identityType == null || identityType != 3) {
                throw new BusinessException("无权查看该储值单");
            }
        }
        return recharge;
    }

    private void scheduleShipping(String rechargeNo) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            wxShippingService.uploadStoreBalanceShippingAsync(rechargeNo);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                wxShippingService.uploadStoreBalanceShippingAsync(rechargeNo);
            }
        });
    }

    private String generateRechargeNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rnd = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "B" + time + rnd;
    }
}
