package com.example.springboottemplate.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.config.WxPayProperties;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.mall.MallCheckoutItemRequest;
import com.example.springboottemplate.dto.mall.MallCheckoutPreviewVO;
import com.example.springboottemplate.dto.mall.MallCheckoutRequest;
import com.example.springboottemplate.dto.mall.MallDiscountSnapshot;
import com.example.springboottemplate.dto.mall.MallFinanceRecord;
import com.example.springboottemplate.dto.mall.MallFinanceSummary;
import com.example.springboottemplate.dto.mall.MallIncomeFlowRow;
import com.example.springboottemplate.dto.mall.MallIncomeFlowVO;
import com.example.springboottemplate.dto.mall.MallPayPrepareVO;
import com.example.springboottemplate.dto.mall.MallRefundRequest;
import com.example.springboottemplate.entity.MallOrder;
import com.example.springboottemplate.entity.MallOrderItem;
import com.example.springboottemplate.entity.MallPromo;
import com.example.springboottemplate.entity.MallUserCoupon;
import com.example.springboottemplate.entity.Product;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.MallOrderItemMapper;
import com.example.springboottemplate.mapper.MallOrderMapper;
import com.example.springboottemplate.mapper.ProductMapper;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.mapper.system.UserMapper;
import com.example.springboottemplate.service.MallCouponService;
import com.example.springboottemplate.service.MallOrderService;
import com.example.springboottemplate.service.MallPromoService;
import com.example.springboottemplate.service.StoreBlacklistService;
import com.example.springboottemplate.service.wx.MallOrderTimeoutService;
import com.example.springboottemplate.service.wx.WxPayClientService;
import com.example.springboottemplate.service.wx.WxShippingService;
import com.example.springboottemplate.utils.JwtUtil;
import com.example.springboottemplate.utils.MallDiscountCalculator;
import com.example.springboottemplate.utils.StoreOpenHelper;
import com.example.springboottemplate.utils.ValidateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import com.wechat.pay.java.service.refund.model.Status;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class MallOrderServiceimpl implements MallOrderService {

    @Autowired
    private MallOrderMapper mallOrderMapper;
    @Autowired
    private MallOrderItemMapper mallOrderItemMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private WxPayClientService wxPayClientService;
    @Autowired
    private WxPayProperties wxPayProperties;
    @Autowired
    private WxShippingService wxShippingService;
    @Autowired
    private StoreBlacklistService storeBlacklistService;
    @Autowired
    private MallPromoService mallPromoService;
    @Autowired
    private MallCouponService mallCouponService;
    @Autowired
    @Lazy
    private MallOrderTimeoutService mallOrderTimeoutService;

    @Override
    public Response checkoutAndPay(MallCheckoutRequest request, HttpServletRequest httpRequest) {
        if (request == null || ValidateUtil.isEmpty(request.getItems())) {
            throw new BusinessException("购物车商品不能为空");
        }
        if (!StringUtils.hasText(request.getContact()) || !request.getContact().matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("请输入正确的手机号码");
        }
        if (!StringUtils.hasText(request.getAddress())) {
            throw new BusinessException("请输入收货地址");
        }

        Claims claims = parseClaims(httpRequest);
        Long userId = jwtUtil.getUserId(claims);
        String username = claims.getSubject();
        if (userId == null) {
            throw new BusinessException("登录状态无效");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!StringUtils.hasText(user.getOpenid()) && !wxPayClientService.isMock()) {
            throw new BusinessException("当前账号未绑定微信，请先微信登录");
        }

        CheckoutQuote quote = quoteCheckout(request, user, false);
        MallDiscountSnapshot discount = quote.discount;
        BigDecimal payAmount = discount.getPayAmount();
        int payAmountFen = discount.getPayAmountFen();
        Date now = new Date();

        String orderNo = generateOrderNo();
        if (quote.coupon != null && !mallCouponService.lockCoupon(quote.coupon.getId(), userId, orderNo)) {
            throw new BusinessException("优惠券已被使用，请重新选择");
        }
        Long orderId = IdWorker.getId();
        MallOrder order = MallOrder.builder()
                .id(orderId)
                .orderNo(orderNo)
                .storeId(quote.storeId)
                .userId(userId)
                .openid(user.getOpenid())
                .contact(request.getContact().trim())
                .address(request.getAddress().trim())
                .remark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null)
                .goodsAmount(quote.goodsAmount)
                .deliveryFee(quote.deliveryFee)
                .promoId(discount.getPromoId())
                .promoDiscount(discount.getPromoDiscount())
                .userCouponId(discount.getUserCouponId())
                .couponDiscount(discount.getCouponDiscount())
                .discountAmount(discount.getDiscountAmount())
                .discountDesc(discount.getDiscountDesc())
                .payAmount(payAmount)
                .payAmountFen(payAmountFen)
                .payStatus(0)
                .createdBy(username)
                .createdTime(now)
                .deleted(0)
                .build();
        mallOrderMapper.addMallOrder(order);

        for (MallOrderItem item : quote.itemEntities) {
            item.setOrderId(orderId);
            item.setOrderNo(orderNo);
        }
        mallOrderItemMapper.batchAdd(quote.itemEntities);

        String description = quote.itemEntities.size() == 1
                ? quote.itemEntities.get(0).getProductName()
                : "商城订单-" + quote.itemEntities.size() + "件商品";
        MallPayPrepareVO payVO = wxPayClientService.createJsapiPrepay(
                orderNo, description, user.getOpenid(), payAmountFen);

        if (!payVO.isMock() && StringUtils.hasText(payVO.getPackageValue())
                && payVO.getPackageValue().startsWith("prepay_id=")) {
            String prepayId = payVO.getPackageValue().substring("prepay_id=".length());
            mallOrderMapper.updatePrepayId(orderNo, prepayId);
        }

        payVO.setOrderNo(orderNo);
        payVO.setPayAmount(payAmount);
        payVO.setPayStatus(0);
        return Response.success(payVO);
    }

    @Override
    public Response previewCheckout(MallCheckoutRequest request, HttpServletRequest httpRequest) {
        if (request == null || ValidateUtil.isEmpty(request.getItems())) {
            throw new BusinessException("购物车商品不能为空");
        }
        Claims claims = parseClaims(httpRequest);
        Long userId = jwtUtil.getUserId(claims);
        if (userId == null) {
            throw new BusinessException("登录状态无效");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        CheckoutQuote quote = quoteCheckout(request, user, true);
        return Response.success(toPreviewVO(quote, user));
    }

    @Override
    public Response queryOrder(String orderNo, HttpServletRequest httpRequest) {
        MallOrder order = requireOwnedOrder(orderNo, httpRequest);
        if (order.getPayStatus() != null && order.getPayStatus() == 0) {
            order = mallOrderTimeoutService.reconcileUnpaid(order, true);
        }
        order.setItems(mallOrderItemMapper.selectByOrderId(order.getId()));
        if (order.getStoreId() != null) {
            Store store = storeMapper.selectByStoreId(order.getStoreId());
            order.setStoreName(store != null ? store.getStoreName() : null);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("mock", wxPayClientService.isMock());
        return Response.success(data);
    }

    @Override
    public Response findMallOrder(Integer payStatus, List<Integer> payStatuses, Long storeId,
                                  Integer pageNum, Integer pageSize, HttpServletRequest httpRequest) {
        Claims claims = parseClaims(httpRequest);
        Long userId = jwtUtil.getUserId(claims);
        if (userId == null) {
            throw new BusinessException("登录状态无效");
        }
        User user = userMapper.selectById(userId);
        Integer identityType = user == null ? null : user.getIdentityType();

        MallOrder query = new MallOrder();
        if (payStatuses != null && !payStatuses.isEmpty()) {
            query.setPayStatuses(payStatuses);
        } else if (payStatus != null) {
            query.setPayStatus(payStatus);
        }

        if (identityType != null && identityType == 2) {
            // 商户：查本店订单（忽略前端传入的 storeId）
            Store storeQuery = new Store();
            storeQuery.setUserId(userId);
            storeQuery.setDeleted(0);
            List<Store> storeList = storeMapper.findStore(storeQuery);
            if (ValidateUtil.isEmpty(storeList) || storeList.get(0).getStoreId() == null) {
                return buildListResponse(Collections.emptyList(), pageNum, pageSize);
            }
            query.setStoreId(storeList.get(0).getStoreId());
        } else if (identityType != null && identityType == 3) {
            // 管理员：可查全部，可按 storeId 过滤
            if (storeId != null) {
                query.setStoreId(storeId);
            }
        } else {
            // 普通用户：仅本人订单
            query.setUserId(userId);
        }

        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        List<MallOrder> list = mallOrderMapper.findMallOrder(query);
        int closed = 0;
        for (int i = 0; i < list.size(); i++) {
            MallOrder item = list.get(i);
            if (item.getPayStatus() != null && item.getPayStatus() == 0
                    && mallOrderTimeoutService.isExpired(item)) {
                MallOrder synced = mallOrderTimeoutService.reconcileUnpaid(item, false);
                list.set(i, synced);
                if (synced == null || synced.getPayStatus() == null || synced.getPayStatus() != 0) {
                    closed++;
                }
            }
        }
        boolean onlyUnpaid = (payStatuses == null || payStatuses.isEmpty()) && payStatus != null && payStatus == 0;
        if (onlyUnpaid && closed > 0) {
            list.removeIf(item -> item == null || item.getPayStatus() == null || item.getPayStatus() != 0);
        }
        list.forEach(item -> {
            item.setItems(mallOrderItemMapper.selectByOrderId(item.getId()));
            if (item.getStoreId() != null) {
                Store store = storeMapper.selectByStoreId(item.getStoreId());
                item.setStoreName(store != null ? store.getStoreName() : null);
            }
        });
        return buildListResponse(list, pageNum, pageSize, (pageNum != null && pageSize != null) ? closed : 0);
    }

    @Override
    public Response incomeFlow(Long storeId, String periodType, Integer year, Integer yearFrom, Integer yearTo,
                               Integer month, HttpServletRequest httpRequest) {
        Store store = requireMerchantOrAdminStore(storeId, httpRequest);

        String dim = normalizePeriodType(periodType);
        LocalDate today = LocalDate.now(SHANGHAI);
        int currentYear = today.getYear();
        PeriodRange range = resolvePeriodRange(dim, year, yearFrom, yearTo, month, currentYear, today.getMonthValue());

        List<MallIncomeFlowRow> raw = mallOrderMapper.sumIncomeFlow(
                store.getStoreId(), dim, toDate(range.start), toDate(range.endExclusive));
        Map<String, MallIncomeFlowRow> byPeriod = new LinkedHashMap<>();
        if (raw != null) {
            for (MallIncomeFlowRow row : raw) {
                if (row == null || !StringUtils.hasText(row.getPeriod())) {
                    continue;
                }
                byPeriod.put(row.getPeriod(), moneyRow(row));
            }
        }

        List<String> periods = buildPeriods(dim, range);
        List<MallIncomeFlowRow> list = new ArrayList<>();
        int totalCount = 0;
        BigDecimal totalPay = BigDecimal.ZERO;
        BigDecimal totalRefund = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        for (String period : periods) {
            MallIncomeFlowRow row = byPeriod.get(period);
            if (row == null) {
                row = emptyRow(period);
            }
            row.setPeriodLabel(formatPeriodLabel(dim, period));
            list.add(row);
            totalCount += row.getOrderCount() == null ? 0 : row.getOrderCount();
            totalPay = totalPay.add(row.getPayAmount());
            totalRefund = totalRefund.add(row.getRefundAmount());
            totalNet = totalNet.add(row.getNetAmount());
        }

        MallIncomeFlowRow summary = new MallIncomeFlowRow();
        summary.setPeriod("summary");
        summary.setPeriodLabel("合计");
        summary.setOrderCount(totalCount);
        summary.setPayAmount(money(totalPay));
        summary.setRefundAmount(money(totalRefund));
        summary.setNetAmount(money(totalNet));

        MallIncomeFlowVO vo = new MallIncomeFlowVO();
        vo.setStoreId(store.getStoreId());
        vo.setStoreName(store.getStoreName());
        vo.setPeriodType(dim);
        vo.setSummary(summary);
        vo.setList(list);
        return Response.success(vo);
    }

    @Override
    public Response financeLedger(Long storeId, String type, String date, Integer pageNum, Integer pageSize,
                                  HttpServletRequest httpRequest) {
        Store store = requireMerchantOrAdminStore(storeId, httpRequest);
        String filterType = "expense".equalsIgnoreCase(type) || "income".equalsIgnoreCase(type)
                ? type.trim().toLowerCase()
                : "all";
        Date startTime = null;
        Date endTime = null;
        if (StringUtils.hasText(date)) {
            LocalDate day;
            try {
                String text = date.trim();
                if (text.length() >= 10) {
                    text = text.substring(0, 10);
                }
                day = LocalDate.parse(text);
            } catch (Exception e) {
                throw new BusinessException("日期格式不正确");
            }
            startTime = toDate(day);
            endTime = toDate(day.plusDays(1));
        }
        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        List<MallFinanceRecord> list = mallOrderMapper.selectFinanceLedger(
                store.getStoreId(), filterType, startTime, endTime);
        if (list == null) {
            list = Collections.emptyList();
        }
        for (MallFinanceRecord row : list) {
            row.setStoreName(store.getStoreName());
        }
        MallFinanceSummary summary = mallOrderMapper.sumFinanceLedger(store.getStoreId(), startTime, endTime);
        if (summary == null) {
            summary = new MallFinanceSummary();
        }
        summary.setIncome(money(summary.getIncome()));
        summary.setExpense(money(summary.getExpense()));

        PageInfo<MallFinanceRecord> pageInfo = new PageInfo<>(list);
        Map<String, Object> data = new HashMap<>();
        data.put("storeId", String.valueOf(store.getStoreId()));
        data.put("storeName", store.getStoreName());
        data.put("summary", summary);
        data.put("list", pageInfo.getList());
        data.put("total", pageInfo.getTotal());
        data.put("pages", pageInfo.getPages());
        data.put("pageNum", pageNum != null ? pageInfo.getPageNum() : 1);
        data.put("pageSize", pageSize != null ? pageInfo.getPageSize() : pageInfo.getSize());
        return Response.success(data);
    }

    private Store requireMerchantOrAdminStore(Long storeId, HttpServletRequest httpRequest) {
        Claims claims = parseClaims(httpRequest);
        Long userId = jwtUtil.getUserId(claims);
        if (userId == null) {
            throw new BusinessException("登录状态无效");
        }
        User user = userMapper.selectById(userId);
        Integer identityType = user == null ? null : user.getIdentityType();
        if (identityType != null && identityType == 2) {
            Store store = findOwnStore(userId);
            if (store == null || store.getStoreId() == null) {
                throw new BusinessException("未找到店铺信息");
            }
            return store;
        }
        if (identityType != null && identityType == 3) {
            if (storeId == null) {
                throw new BusinessException("请选择店铺");
            }
            Store store = storeMapper.selectByStoreId(storeId);
            if (store == null || (store.getDeleted() != null && store.getDeleted() == 1)) {
                throw new BusinessException("未找到店铺信息");
            }
            return store;
        }
        throw new BusinessException("仅商家或管理员可查询流水");
    }

    private Store findOwnStore(Long userId) {
        Store storeQuery = new Store();
        storeQuery.setUserId(userId);
        storeQuery.setDeleted(0);
        List<Store> storeList = storeMapper.findStore(storeQuery);
        if (ValidateUtil.isEmpty(storeList)) {
            return null;
        }
        return storeList.get(0);
    }

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final String[] QUARTER_LABELS = {"", "第一季度", "第二季度", "第三季度", "第四季度"};

    private static String normalizePeriodType(String periodType) {
        if (!StringUtils.hasText(periodType)) {
            return "month";
        }
        String dim = periodType.trim().toLowerCase();
        if ("year".equals(dim) || "quarter".equals(dim) || "month".equals(dim) || "day".equals(dim)) {
            return dim;
        }
        throw new BusinessException("统计维度不正确");
    }

    private static PeriodRange resolvePeriodRange(String dim, Integer year, Integer yearFrom, Integer yearTo,
                                                  Integer month, int currentYear, int currentMonth) {
        PeriodRange range = new PeriodRange();
        if ("year".equals(dim)) {
            int from = yearFrom != null ? yearFrom : (year != null ? year : currentYear);
            int to = yearTo != null ? yearTo : from;
            if (from > to) {
                int tmp = from;
                from = to;
                to = tmp;
            }
            if (from < 2000 || to > 2100 || to - from > 20) {
                throw new BusinessException("年份范围不正确");
            }
            range.yearFrom = from;
            range.yearTo = to;
            range.start = LocalDate.of(from, 1, 1);
            range.endExclusive = LocalDate.of(to + 1, 1, 1);
            return range;
        }
        int y = year != null ? year : currentYear;
        if (y < 2000 || y > 2100) {
            throw new BusinessException("年份不正确");
        }
        range.yearFrom = y;
        range.yearTo = y;
        if ("day".equals(dim)) {
            int m = month != null ? month : currentMonth;
            if (m < 1 || m > 12) {
                throw new BusinessException("月份不正确");
            }
            range.month = m;
            range.start = LocalDate.of(y, m, 1);
            range.endExclusive = range.start.plusMonths(1);
            return range;
        }
        range.start = LocalDate.of(y, 1, 1);
        range.endExclusive = LocalDate.of(y + 1, 1, 1);
        return range;
    }

    private static List<String> buildPeriods(String dim, PeriodRange range) {
        List<String> periods = new ArrayList<>();
        if ("year".equals(dim)) {
            for (int y = range.yearFrom; y <= range.yearTo; y++) {
                periods.add(String.valueOf(y));
            }
            return periods;
        }
        if ("quarter".equals(dim)) {
            for (int q = 1; q <= 4; q++) {
                periods.add(range.yearFrom + "-Q" + q);
            }
            return periods;
        }
        if ("month".equals(dim)) {
            for (int m = 1; m <= 12; m++) {
                periods.add(String.format("%d-%02d", range.yearFrom, m));
            }
            return periods;
        }
        YearMonth ym = YearMonth.of(range.yearFrom, range.month);
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            periods.add(String.format("%d-%02d-%02d", range.yearFrom, range.month, d));
        }
        return periods;
    }

    private static String formatPeriodLabel(String dim, String period) {
        if ("year".equals(dim)) {
            return period + "年";
        }
        if ("quarter".equals(dim)) {
            int qIndex = period.lastIndexOf("-Q");
            int q = 0;
            try {
                q = Integer.parseInt(period.substring(qIndex + 2));
            } catch (Exception ignored) {
                // fall through
            }
            String year = qIndex > 0 ? period.substring(0, qIndex) : period;
            String qLabel = q >= 1 && q <= 4 ? QUARTER_LABELS[q] : period;
            return year + "年" + qLabel;
        }
        if ("month".equals(dim) && period.length() >= 7) {
            return period.substring(0, 4) + "年" + Integer.parseInt(period.substring(5, 7)) + "月";
        }
        if (period.length() >= 10) {
            return period.substring(0, 4) + "年"
                    + Integer.parseInt(period.substring(5, 7)) + "月"
                    + Integer.parseInt(period.substring(8, 10)) + "日";
        }
        return period;
    }

    private static MallIncomeFlowRow emptyRow(String period) {
        MallIncomeFlowRow row = new MallIncomeFlowRow();
        row.setPeriod(period);
        row.setOrderCount(0);
        row.setPayAmount(money(BigDecimal.ZERO));
        row.setRefundAmount(money(BigDecimal.ZERO));
        row.setNetAmount(money(BigDecimal.ZERO));
        return row;
    }

    private static MallIncomeFlowRow moneyRow(MallIncomeFlowRow row) {
        if (row.getOrderCount() == null) {
            row.setOrderCount(0);
        }
        row.setPayAmount(money(row.getPayAmount()));
        row.setRefundAmount(money(row.getRefundAmount()));
        row.setNetAmount(money(row.getNetAmount()));
        return row;
    }

    private static BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private static Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(SHANGHAI).toInstant());
    }

    private static class PeriodRange {
        int yearFrom;
        int yearTo;
        int month;
        LocalDate start;
        LocalDate endExclusive;
    }

    private Response buildListResponse(List<MallOrder> list, Integer pageNum, Integer pageSize) {
        return buildListResponse(list, pageNum, pageSize, 0);
    }

    private Response buildListResponse(List<MallOrder> list, Integer pageNum, Integer pageSize, int closedOnPage) {
        PageInfo<MallOrder> pageInfo = new PageInfo<>(list);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getList());
        long total = pageInfo.getTotal();
        if (closedOnPage > 0) {
            total = Math.max(0, total - closedOnPage);
        }
        data.put("total", total);
        data.put("pages", pageInfo.getPages());
        data.put("pageNum", pageNum != null ? pageInfo.getPageNum() : 1);
        data.put("pageSize", pageSize != null ? pageInfo.getPageSize() : pageInfo.getTotal());
        return Response.success(data);
    }

    @Override
    public Response mockConfirmPay(String orderNo, HttpServletRequest httpRequest) {
        if (!wxPayClientService.isMock()) {
            throw new BusinessException("仅 mock 模式可用");
        }
        MallOrder order = requireOwnedOrder(orderNo, httpRequest);
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
            return Response.success(order);
        }
        if (order.getPayStatus() != null && order.getPayStatus() != 0) {
            throw new BusinessException("订单状态不允许支付");
        }
        markOrderPaid(orderNo, "MOCK" + orderNo, new Date());
        MallOrder paid = mallOrderMapper.selectByOrderNo(orderNo);
        paid.setItems(mallOrderItemMapper.selectByOrderId(paid.getId()));
        return Response.success(paid);
    }

    @Override
    public String handleWxPayNotify(HttpServletRequest request, String body) {
        try {
            Transaction transaction = wxPayClientService.parseNotify(
                    body,
                    request.getHeader("Wechatpay-Serial"),
                    request.getHeader("Wechatpay-Nonce"),
                    request.getHeader("Wechatpay-Signature"),
                    request.getHeader("Wechatpay-Timestamp"),
                    request.getHeader("Wechatpay-Signature-Type")
            );
            if (transaction.getTradeState() == Transaction.TradeStateEnum.SUCCESS) {
                markOrderPaid(transaction.getOutTradeNo(), transaction.getTransactionId(), new Date());
            }
            return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
        } catch (Exception e) {
            return "{\"code\":\"FAIL\",\"message\":\"" + escapeJson(e.getMessage()) + "\"}";
        }
    }

    @Override
    public Response refund(MallRefundRequest request, HttpServletRequest httpRequest) {
        if (request == null || !StringUtils.hasText(request.getOrderNo())) {
            throw new BusinessException("订单号不能为空");
        }
        Claims claims = parseClaims(httpRequest);
        Long userId = jwtUtil.getUserId(claims);
        String operator = claims.getSubject();
        if (userId == null) {
            throw new BusinessException("登录状态无效");
        }

        MallOrder order = mallOrderMapper.selectByOrderNo(request.getOrderNo().trim());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        requireMerchantOrAdmin(order, userId);

        Integer payStatus = order.getPayStatus();
        if (payStatus == null || (payStatus != 1 && payStatus != 4)) {
            if (payStatus != null && payStatus == 3) {
                throw new BusinessException("订单退款处理中，请稍后再试");
            }
            if (payStatus != null && payStatus == 5) {
                throw new BusinessException("订单已全额退款");
            }
            throw new BusinessException("当前订单状态不可退款");
        }
        if (order.getPayAmountFen() == null || order.getPayAmountFen() <= 0) {
            throw new BusinessException("订单支付金额异常");
        }

        int alreadyRefunded = order.getRefundAmountFen() == null ? 0 : order.getRefundAmountFen();
        int remainFen = order.getPayAmountFen() - alreadyRefunded;
        if (remainFen <= 0) {
            throw new BusinessException("可退金额不足");
        }

        boolean fullRefund = Boolean.TRUE.equals(request.getFullRefund());
        int refundFen;
        if (fullRefund) {
            refundFen = remainFen;
        } else if (request.getRefundAmountFen() != null) {
            refundFen = request.getRefundAmountFen();
        } else if (request.getRefundAmount() != null) {
            refundFen = request.getRefundAmount().movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValueExact();
        } else {
            throw new BusinessException("请指定全额退款或部分退款金额");
        }
        if (refundFen <= 0) {
            throw new BusinessException("退款金额必须大于0");
        }
        if (refundFen > remainFen) {
            throw new BusinessException("退款金额不能超过可退金额（剩余"
                    + BigDecimal.valueOf(remainFen).movePointLeft(2).toPlainString() + "元）");
        }

        String reason = StringUtils.hasText(request.getReason()) ? request.getReason().trim() : "商家协商退款";
        String outRefundNo = generateRefundNo();

        int locked = mallOrderMapper.markRefunding(order.getOrderNo(), refundFen, outRefundNo, reason, operator);
        if (locked <= 0) {
            throw new BusinessException("订单状态已变更，请刷新后重试");
        }

        try {
            Refund refund = wxPayClientService.createRefund(
                    order.getOrderNo(), outRefundNo, order.getPayAmountFen(), refundFen, reason);
            Status status = refund.getStatus();
            String wxRefundId = refund.getRefundId();

            if (status == Status.SUCCESS) {
                applyRefundSuccess(order.getOrderNo(), outRefundNo, wxRefundId, refundFen, new Date(), false);
            } else if (status == Status.CLOSED || status == Status.ABNORMAL) {
                mallOrderMapper.markRefundFailed(order.getOrderNo(), outRefundNo);
                throw new BusinessException("微信退款未成功: " + status);
            }
            // PROCESSING：保持退款中，等回调

            MallOrder latest = mallOrderMapper.selectByOrderNo(order.getOrderNo());
            latest.setItems(mallOrderItemMapper.selectByOrderId(latest.getId()));
            Map<String, Object> data = new HashMap<>();
            data.put("order", latest);
            data.put("outRefundNo", outRefundNo);
            data.put("wxRefundId", wxRefundId);
            data.put("refundAmountFen", refundFen);
            data.put("refundAmount", BigDecimal.valueOf(refundFen).movePointLeft(2));
            data.put("refundStatus", status == null ? null : status.name());
            data.put("mock", wxPayClientService.isMock());
            return Response.success(data);
        } catch (BusinessException e) {
            mallOrderMapper.markRefundFailed(order.getOrderNo(), outRefundNo);
            throw e;
        } catch (Exception e) {
            mallOrderMapper.markRefundFailed(order.getOrderNo(), outRefundNo);
            throw new BusinessException("发起退款失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String handleWxRefundNotify(HttpServletRequest request, String body) {
        try {
            RefundNotification notification = wxPayClientService.parseRefundNotify(
                    body,
                    request.getHeader("Wechatpay-Serial"),
                    request.getHeader("Wechatpay-Nonce"),
                    request.getHeader("Wechatpay-Signature"),
                    request.getHeader("Wechatpay-Timestamp"),
                    request.getHeader("Wechatpay-Signature-Type")
            );
            String outRefundNo = notification.getOutRefundNo();
            String orderNo = notification.getOutTradeNo();
            Status status = notification.getRefundStatus();
            if (status == Status.SUCCESS) {
                int refundFen = 0;
                if (notification.getAmount() != null && notification.getAmount().getRefund() != null) {
                    refundFen = notification.getAmount().getRefund().intValue();
                } else {
                    MallOrder order = mallOrderMapper.selectByOutRefundNo(outRefundNo);
                    if (order != null && order.getPendingRefundFen() != null) {
                        refundFen = order.getPendingRefundFen();
                    }
                }
                if (refundFen > 0 && StringUtils.hasText(orderNo)) {
                    applyRefundSuccess(orderNo, outRefundNo, notification.getRefundId(), refundFen, new Date(), false);
                }
            } else if (status == Status.CLOSED || status == Status.ABNORMAL) {
                if (StringUtils.hasText(orderNo) && StringUtils.hasText(outRefundNo)) {
                    mallOrderMapper.markRefundFailed(orderNo, outRefundNo);
                }
            }
            return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
        } catch (Exception e) {
            return "{\"code\":\"FAIL\",\"message\":\"" + escapeJson(e.getMessage()) + "\"}";
        }
    }

    private void applyRefundSuccess(String orderNo, String outRefundNo, String wxRefundId,
                                    int refundFen, Date refundTime, boolean allowDirect) {
        MallOrder before = mallOrderMapper.selectByOrderNo(orderNo);
        if (before == null) {
            return;
        }
        // 幂等：同一退款单已成功计入则跳过
        if (StringUtils.hasText(outRefundNo)
                && outRefundNo.equals(before.getOutRefundNo())
                && before.getPayStatus() != null
                && before.getPayStatus() != 3
                && before.getPendingRefundFen() == null
                && before.getWxRefundId() != null
                && before.getWxRefundId().equals(wxRefundId)) {
            return;
        }
        int already = before.getRefundAmountFen() == null ? 0 : before.getRefundAmountFen();
        boolean willFull = already + refundFen >= (before.getPayAmountFen() == null ? 0 : before.getPayAmountFen());

        int rows = mallOrderMapper.markRefundSuccess(
                orderNo, refundFen, outRefundNo, wxRefundId, refundTime, allowDirect ? 1 : 0);
        if (rows <= 0) {
            return;
        }
        // 全额退款成功后回滚库存/销量
        if (willFull) {
            restoreStock(before);
        }
    }

    private void restoreStock(MallOrder order) {
        List<MallOrderItem> items = mallOrderItemMapper.selectByOrderId(order.getId());
        Date now = new Date();
        for (MallOrderItem item : items) {
            Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                    .eq(Product::getProductId, item.getProductId())
                    .last("limit 1"));
            if (product == null) {
                continue;
            }
            int stock = product.getProductNum() == null ? 0 : product.getProductNum();
            int sale = product.getSaleNum() == null ? 0 : product.getSaleNum();
            int qty = item.getQuantity() == null ? 0 : item.getQuantity();
            product.setProductNum(stock + qty);
            product.setSaleNum(Math.max(0, sale - qty));
            product.setUpdateTime(now);
            productMapper.updateProduct(product);
        }
    }

    /** 仅本店商户或管理员可退款 */
    private void requireMerchantOrAdmin(MallOrder order, Long userId) {
        User user = userMapper.selectById(userId);
        Integer identityType = user == null ? null : user.getIdentityType();
        if (identityType != null && identityType == 3) {
            return;
        }
        if (identityType != null && identityType == 2 && order.getStoreId() != null) {
            Store storeQuery = new Store();
            storeQuery.setUserId(userId);
            storeQuery.setDeleted(0);
            List<Store> storeList = storeMapper.findStore(storeQuery);
            if (!ValidateUtil.isEmpty(storeList)
                    && storeList.get(0).getStoreId() != null
                    && storeList.get(0).getStoreId().equals(order.getStoreId())) {
                return;
            }
        }
        throw new BusinessException("仅本店商家或管理员可发起退款");
    }

    @Override
    public void applyPaidIfUnpaid(String orderNo, String transactionId) {
        markOrderPaid(orderNo, transactionId, new Date());
    }

    @Override
    public void closeIfUnpaid(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            return;
        }
        mallOrderMapper.markClosed(orderNo.trim(), new Date());
        mallCouponService.unlockByOrderNo(orderNo.trim());
    }

    private void markOrderPaid(String orderNo, String transactionId, Date paidTime) {
        MallOrder order = mallOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return;
        }
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
            return;
        }
        int rows = mallOrderMapper.markPaid(orderNo, transactionId, paidTime);
        if (rows <= 0) {
            return;
        }
        mallCouponService.markUsedByOrderNo(orderNo);
        // 扣库存、加销量（幂等：仅首次 markPaid 成功时执行）
        List<MallOrderItem> items = mallOrderItemMapper.selectByOrderId(order.getId());
        for (MallOrderItem item : items) {
            Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                    .eq(Product::getProductId, item.getProductId())
                    .last("limit 1"));
            if (product == null) {
                continue;
            }
            int stock = product.getProductNum() == null ? 0 : product.getProductNum();
            int sale = product.getSaleNum() == null ? 0 : product.getSaleNum();
            int qty = item.getQuantity() == null ? 0 : item.getQuantity();
            product.setProductNum(Math.max(0, stock - qty));
            product.setSaleNum(sale + qty);
            product.setUpdateTime(paidTime);
            productMapper.updateProduct(product);
        }
        scheduleWxShippingAfterCommit(orderNo);
    }

    /** 事务提交后再报发货，避免挡住支付回调、也避免未提交订单被微信查不到 */
    private void scheduleWxShippingAfterCommit(String orderNo) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            wxShippingService.uploadShippingAsync(orderNo);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                wxShippingService.uploadShippingAsync(orderNo);
            }
        });
    }

    private MallOrder requireOwnedOrder(String orderNo, HttpServletRequest httpRequest) {
        if (!StringUtils.hasText(orderNo)) {
            throw new BusinessException("订单号不能为空");
        }
        Claims claims = parseClaims(httpRequest);
        Long userId = jwtUtil.getUserId(claims);
        MallOrder order = mallOrderMapper.selectByOrderNo(orderNo.trim());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (userId == null) {
            throw new BusinessException("无权查看该订单");
        }
        User user = userMapper.selectById(userId);
        Integer identityType = user == null ? null : user.getIdentityType();
        // 管理员可查全部
        if (identityType != null && identityType == 3) {
            return order;
        }
        // 下单人可查
        if (order.getUserId() != null && order.getUserId().equals(userId)) {
            return order;
        }
        // 本店商户可查
        if (identityType != null && identityType == 2 && order.getStoreId() != null) {
            Store storeQuery = new Store();
            storeQuery.setUserId(userId);
            storeQuery.setDeleted(0);
            List<Store> storeList = storeMapper.findStore(storeQuery);
            if (!ValidateUtil.isEmpty(storeList)
                    && storeList.get(0).getStoreId() != null
                    && storeList.get(0).getStoreId().equals(order.getStoreId())) {
                return order;
            }
        }
        throw new BusinessException("无权查看该订单");
    }

    private CheckoutQuote quoteCheckout(MallCheckoutRequest request, User user, boolean preview) {
        List<MallOrderItem> itemEntities = new ArrayList<>();
        BigDecimal goodsAmount = BigDecimal.ZERO;
        Long storeId = null;
        Date now = new Date();

        for (MallCheckoutItemRequest line : request.getItems()) {
            if (line == null || line.getProductId() == null || line.getQuantity() == null || line.getQuantity() <= 0) {
                throw new BusinessException("商品或数量无效");
            }
            Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                    .eq(Product::getProductId, line.getProductId())
                    .last("limit 1"));
            if (product == null) {
                throw new BusinessException("商品不存在: " + line.getProductId());
            }
            if (product.getProductStatus() == null || product.getProductStatus() != 1) {
                throw new BusinessException("商品已下架: " + product.getProductName());
            }
            if (product.getProductNum() == null || product.getProductNum() < line.getQuantity()) {
                throw new BusinessException("库存不足: " + product.getProductName());
            }
            if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("商品价格异常: " + product.getProductName());
            }

            Long lineStoreId = parseStoreId(product.getStoreId());
            if (lineStoreId != null) {
                if (storeId == null) {
                    storeId = lineStoreId;
                } else if (!storeId.equals(lineStoreId)) {
                    throw new BusinessException("暂不支持跨店结算，请分店铺下单");
                }
            }

            BigDecimal lineAmount = product.getPrice()
                    .multiply(BigDecimal.valueOf(line.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            goodsAmount = goodsAmount.add(lineAmount);

            itemEntities.add(MallOrderItem.builder()
                    .id(IdWorker.getId())
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .productImg(product.getProductImg())
                    .price(product.getPrice())
                    .quantity(line.getQuantity())
                    .amount(lineAmount)
                    .createdTime(now)
                    .deleted(0)
                    .build());
        }

        Store store = null;
        if (storeId != null) {
            store = storeMapper.selectByStoreId(storeId);
            if (store != null && !StoreOpenHelper.isAcceptingOrders(store)) {
                throw new BusinessException(StoreOpenHelper.rejectMessage(store));
            }
            if (storeBlacklistService.isBlacklisted(storeId, user.getId(), user.getPhone(), request.getContact())) {
                throw new BusinessException("您已被该店铺限制下单");
            }
        }

        BigDecimal deliveryFee = resolveStoreDeliveryFee(store);
        MallPromo promo = mallPromoService.findActiveByStoreId(storeId);
        String couponError = null;
        MallUserCoupon coupon = null;
        if (request.getUserCouponId() != null) {
            try {
                coupon = mallCouponService.requireUsableCoupon(request.getUserCouponId(), user.getId(), storeId);
            } catch (BusinessException e) {
                if (!preview) {
                    throw e;
                }
                couponError = e.getMessage();
            }
        }

        MallDiscountSnapshot discount;
        try {
            discount = MallDiscountCalculator.compute(goodsAmount, deliveryFee, promo, coupon);
        } catch (BusinessException e) {
            if (!preview || coupon == null) {
                throw e;
            }
            couponError = e.getMessage();
            coupon = null;
            discount = MallDiscountCalculator.compute(goodsAmount, deliveryFee, promo, null);
        }

        CheckoutQuote quote = new CheckoutQuote();
        quote.itemEntities = itemEntities;
        quote.goodsAmount = goodsAmount.setScale(2, RoundingMode.HALF_UP);
        quote.storeId = storeId;
        quote.store = store;
        quote.deliveryFee = deliveryFee;
        quote.promo = promo;
        quote.coupon = coupon;
        quote.discount = discount;
        quote.couponError = couponError;
        return quote;
    }

    private MallCheckoutPreviewVO toPreviewVO(CheckoutQuote quote, User user) {
        MallDiscountSnapshot discount = quote.discount;
        List<MallUserCoupon> coupons = annotateCheckoutCoupons(
                mallCouponService.listUnusedByUserAndStore(user.getId(), quote.storeId),
                quote.goodsAmount,
                quote.deliveryFee,
                quote.promo);
        return MallCheckoutPreviewVO.builder()
                .goodsAmount(quote.goodsAmount)
                .deliveryFee(quote.deliveryFee)
                .promoId(quote.promo == null ? null : quote.promo.getId())
                .promoDiscount(discount.getPromoDiscount())
                .promoText(quote.promo == null ? null : MallDiscountCalculator.tierText(quote.promo.getTiers()))
                .userCouponId(discount.getUserCouponId())
                .couponDiscount(discount.getCouponDiscount())
                .discountAmount(discount.getDiscountAmount())
                .discountDesc(discount.getDiscountDesc())
                .payAmount(discount.getPayAmount())
                .couponError(quote.couponError)
                .coupons(coupons)
                .build();
    }

    private List<MallUserCoupon> annotateCheckoutCoupons(List<MallUserCoupon> coupons,
                                                         BigDecimal goodsAmount,
                                                         BigDecimal deliveryFee,
                                                         MallPromo promo) {
        if (ValidateUtil.isEmpty(coupons)) {
            return Collections.emptyList();
        }
        List<MallUserCoupon> usable = new ArrayList<>();
        List<MallUserCoupon> unusable = new ArrayList<>();
        BigDecimal goods = goodsAmount == null ? BigDecimal.ZERO : goodsAmount;
        for (MallUserCoupon coupon : coupons) {
            BigDecimal threshold = coupon.getThresholdAmount() == null ? BigDecimal.ZERO : coupon.getThresholdAmount();
            if (goods.compareTo(threshold) < 0) {
                coupon.setUsable(false);
                coupon.setDisableReason("商品满" + threshold.stripTrailingZeros().toPlainString() + "可用");
                unusable.add(coupon);
                continue;
            }
            MallDiscountSnapshot snapshot = MallDiscountCalculator.tryCompute(goods, deliveryFee, promo, coupon);
            if (snapshot == null || snapshot.getCouponDiscount() == null
                    || snapshot.getCouponDiscount().compareTo(BigDecimal.ZERO) <= 0) {
                coupon.setUsable(false);
                coupon.setDisableReason("当前订单无法使用该券");
                unusable.add(coupon);
                continue;
            }
            coupon.setUsable(true);
            coupon.setDisableReason(null);
            usable.add(coupon);
        }
        usable.addAll(unusable);
        return usable;
    }

    private static class CheckoutQuote {
        private List<MallOrderItem> itemEntities;
        private BigDecimal goodsAmount;
        private Long storeId;
        private Store store;
        private BigDecimal deliveryFee;
        private MallPromo promo;
        private MallUserCoupon coupon;
        private MallDiscountSnapshot discount;
        private String couponError;
    }

    private Claims parseClaims(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
            throw new BusinessException("未登录");
        }
        return jwtUtil.parseToken(auth.substring(7));
    }

    private BigDecimal resolveStoreDeliveryFee(Store store) {
        BigDecimal fee = store != null ? store.getDeliveryFee() : null;
        if (fee == null && wxPayProperties.getDeliveryFee() != null) {
            fee = wxPayProperties.getDeliveryFee();
        }
        if (fee == null || fee.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return fee.setScale(2, RoundingMode.HALF_UP);
    }

    private Long parseStoreId(String storeId) {
        if (!StringUtils.hasText(storeId)) {
            return null;
        }
        try {
            return Long.valueOf(storeId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String generateOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rnd = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "M" + time + rnd;
    }

    private String generateRefundNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rnd = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "R" + time + rnd;
    }

    private String escapeJson(String msg) {
        if (msg == null) {
            return "error";
        }
        return msg.replace("\"", "'");
    }
}
