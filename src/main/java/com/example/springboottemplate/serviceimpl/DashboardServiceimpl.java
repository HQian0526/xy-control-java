package com.example.springboottemplate.serviceimpl;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.dashboard.DashboardChannelRow;
import com.example.springboottemplate.dto.dashboard.DashboardDayRow;
import com.example.springboottemplate.dto.dashboard.DashboardHourRow;
import com.example.springboottemplate.dto.dashboard.DashboardOverviewVO;
import com.example.springboottemplate.dto.dashboard.DashboardStoreRow;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.mapper.DashboardMapper;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.service.DashboardService;
import com.example.springboottemplate.utils.StoreAuthHelper;
import com.example.springboottemplate.utils.StoreOpenHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceimpl implements DashboardService {

    private static final ZoneId SHANGHAI = StoreOpenHelper.SHANGHAI;
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Autowired
    private DashboardMapper dashboardMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private StoreAuthHelper storeAuthHelper;

    @Override
    public Response overview(HttpServletRequest request) {
        StoreAuthHelper.Operator operator = storeAuthHelper.requireVerifyOperator(request);
        Long storeId = null;
        String storeName = null;
        if (operator.isStaff()) {
            storeId = operator.getStaffStoreId();
        } else if (operator.isMerchant()) {
            storeId = storeAuthHelper.resolveCurrentOwnedStoreId(operator.getUserId(), operator.getHeaderStoreId());
        }
        if (storeId != null) {
            Store store = storeMapper.selectByStoreId(storeId);
            storeName = store == null ? null : store.getStoreName();
        }

        LocalDate today = LocalDate.now(SHANGHAI);
        if (!operator.isAdmin() && storeId == null) {
            return Response.success(emptyOverview(operator, storeName, today));
        }
        Date todayStart = toDate(today);
        Date tomorrowStart = toDate(today.plusDays(1));
        Date yesterdayStart = toDate(today.minusDays(1));
        Date thisMonthStart = toDate(today.withDayOfMonth(1));
        Date lastMonthStart = toDate(today.minusMonths(1).withDayOfMonth(1));
        Date lastMonthSameEnd = toDate(today.minusMonths(1).plusDays(1));
        Date trendStart = toDate(today.minusDays(13));

        BigDecimal todaySales = money(dashboardMapper.sumNetSales(storeId, todayStart, tomorrowStart));
        BigDecimal yesterdaySales = money(dashboardMapper.sumNetSales(storeId, yesterdayStart, todayStart));
        Integer todayOrders = count(dashboardMapper.countPaidOrders(storeId, todayStart, tomorrowStart));
        Integer yesterdayOrders = count(dashboardMapper.countPaidOrders(storeId, yesterdayStart, todayStart));
        BigDecimal monthSales = money(dashboardMapper.sumNetSales(storeId, thisMonthStart, tomorrowStart));
        BigDecimal lastMonthSales = money(dashboardMapper.sumNetSales(storeId, lastMonthStart, lastMonthSameEnd));
        Integer monthOrders = count(dashboardMapper.countPaidOrders(storeId, thisMonthStart, tomorrowStart));
        Integer todayCustomers = count(dashboardMapper.countCustomers(storeId, todayStart, tomorrowStart));
        Integer yesterdayCustomers = count(dashboardMapper.countCustomers(storeId, yesterdayStart, todayStart));
        Integer todayNew = count(dashboardMapper.countNewCustomers(storeId, todayStart, tomorrowStart));
        Integer yesterdayNew = count(dashboardMapper.countNewCustomers(storeId, yesterdayStart, todayStart));
        BigDecimal todayRecharge = money(dashboardMapper.sumRecharge(storeId, todayStart, tomorrowStart));
        BigDecimal yesterdayRecharge = money(dashboardMapper.sumRecharge(storeId, yesterdayStart, todayStart));
        BigDecimal todayRefund = money(dashboardMapper.sumRefund(storeId, todayStart, tomorrowStart));

        List<DashboardDayRow> salesTrend = nvlList(dashboardMapper.listSalesTrend(storeId, trendStart, tomorrowStart));
        List<DashboardDayRow> customerTrend = nvlList(dashboardMapper.listCustomerTrend(storeId, trendStart, tomorrowStart));
        List<DashboardDayRow> newTrend = nvlList(dashboardMapper.listNewCustomerTrend(storeId, trendStart, tomorrowStart));
        List<DashboardDayRow> trend = mergeTrend(today.minusDays(13), today, salesTrend, customerTrend, newTrend);

        List<DashboardHourRow> hourSales = fillHours(nvlList(dashboardMapper.listHourSales(storeId, todayStart, tomorrowStart)));
        List<DashboardChannelRow> channels = fillChannelNames(nvlList(dashboardMapper.listPayChannels(storeId, todayStart, tomorrowStart)));
        List<DashboardStoreRow> topStores = operator.isAdmin()
                ? nvlList(dashboardMapper.listTopStores(todayStart, tomorrowStart, 5))
                : new ArrayList<>();

        DashboardOverviewVO vo = DashboardOverviewVO.builder()
                .storeId(storeId)
                .storeName(storeName)
                .identityType(operator.getIdentityType())
                .today(today.format(DAY_FMT))
                .todaySales(todaySales)
                .yesterdaySales(yesterdaySales)
                .todaySalesRate(rate(todaySales, yesterdaySales))
                .todayOrderCount(todayOrders)
                .yesterdayOrderCount(yesterdayOrders)
                .todayOrderRate(rate(todayOrders, yesterdayOrders))
                .monthSales(monthSales)
                .lastMonthSales(lastMonthSales)
                .monthSalesRate(rate(monthSales, lastMonthSales))
                .monthOrderCount(monthOrders)
                .todayCustomers(todayCustomers)
                .yesterdayCustomers(yesterdayCustomers)
                .todayCustomerRate(rate(todayCustomers, yesterdayCustomers))
                .todayNewCustomers(todayNew)
                .yesterdayNewCustomers(yesterdayNew)
                .todayNewCustomerRate(rate(todayNew, yesterdayNew))
                .todayRecharge(todayRecharge)
                .yesterdayRecharge(yesterdayRecharge)
                .todayRechargeRate(rate(todayRecharge, yesterdayRecharge))
                .todayRefund(todayRefund)
                .trend(trend)
                .hourSales(hourSales)
                .payChannels(channels)
                .topStores(topStores)
                .build();
        return Response.success(vo);
    }

    private List<DashboardDayRow> mergeTrend(LocalDate from, LocalDate to,
                                             List<DashboardDayRow> sales,
                                             List<DashboardDayRow> customers,
                                             List<DashboardDayRow> news) {
        Map<String, DashboardDayRow> salesMap = byDay(sales);
        Map<String, DashboardDayRow> customerMap = byDay(customers);
        Map<String, DashboardDayRow> newMap = byDay(news);
        List<DashboardDayRow> list = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            String key = d.format(DAY_FMT);
            DashboardDayRow sale = salesMap.get(key);
            DashboardDayRow customer = customerMap.get(key);
            DashboardDayRow neu = newMap.get(key);
            list.add(DashboardDayRow.builder()
                    .day(key)
                    .sales(sale == null ? ZERO : money(sale.getSales()))
                    .orderCount(sale == null ? 0 : count(sale.getOrderCount()))
                    .customers(customer == null ? 0 : count(customer.getCustomers()))
                    .newCustomers(neu == null ? 0 : count(neu.getNewCustomers()))
                    .build());
        }
        return list;
    }

    private Map<String, DashboardDayRow> byDay(List<DashboardDayRow> rows) {
        Map<String, DashboardDayRow> map = new HashMap<>();
        for (DashboardDayRow row : rows) {
            if (row != null && row.getDay() != null) {
                map.put(row.getDay(), row);
            }
        }
        return map;
    }

    private List<DashboardHourRow> fillHours(List<DashboardHourRow> raw) {
        Map<String, DashboardHourRow> map = new HashMap<>();
        for (DashboardHourRow row : raw) {
            if (row != null && row.getHour() != null) {
                map.put(row.getHour(), row);
            }
        }
        List<DashboardHourRow> list = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            String hour = i < 10 ? "0" + i : String.valueOf(i);
            DashboardHourRow hit = map.get(hour);
            if (hit == null) {
                hit = map.get(String.valueOf(i));
            }
            list.add(DashboardHourRow.builder()
                    .hour(hour)
                    .sales(hit == null ? ZERO : money(hit.getSales()))
                    .orderCount(hit == null ? 0 : count(hit.getOrderCount()))
                    .build());
        }
        return list;
    }

    private List<DashboardChannelRow> fillChannelNames(List<DashboardChannelRow> rows) {
        for (DashboardChannelRow row : rows) {
            if (row == null) {
                continue;
            }
            row.setAmount(money(row.getAmount()));
            row.setOrderCount(count(row.getOrderCount()));
            Integer channel = row.getChannel();
            if (channel != null && channel == 2) {
                row.setChannelName("余额支付");
            } else if (channel != null && channel == 3) {
                row.setChannelName("无需支付");
            } else {
                row.setChannelName("微信支付");
            }
        }
        return rows;
    }

    private DashboardOverviewVO emptyOverview(StoreAuthHelper.Operator operator, String storeName, LocalDate today) {
        return DashboardOverviewVO.builder()
                .storeId(null)
                .storeName(storeName)
                .identityType(operator.getIdentityType())
                .today(today.format(DAY_FMT))
                .todaySales(ZERO)
                .yesterdaySales(ZERO)
                .todaySalesRate(ZERO)
                .todayOrderCount(0)
                .yesterdayOrderCount(0)
                .todayOrderRate(ZERO)
                .monthSales(ZERO)
                .lastMonthSales(ZERO)
                .monthSalesRate(ZERO)
                .monthOrderCount(0)
                .todayCustomers(0)
                .yesterdayCustomers(0)
                .todayCustomerRate(ZERO)
                .todayNewCustomers(0)
                .yesterdayNewCustomers(0)
                .todayNewCustomerRate(ZERO)
                .todayRecharge(ZERO)
                .yesterdayRecharge(ZERO)
                .todayRechargeRate(ZERO)
                .todayRefund(ZERO)
                .trend(mergeTrend(today.minusDays(13), today, new ArrayList<>(), new ArrayList<>(), new ArrayList<>()))
                .hourSales(fillHours(new ArrayList<>()))
                .payChannels(new ArrayList<>())
                .topStores(new ArrayList<>())
                .build();
    }

    private BigDecimal rate(BigDecimal current, BigDecimal previous) {
        BigDecimal now = money(current);
        BigDecimal prev = money(previous);
        if (prev.compareTo(BigDecimal.ZERO) == 0) {
            return now.compareTo(BigDecimal.ZERO) == 0 ? ZERO : new BigDecimal("100.0");
        }
        return now.subtract(prev).multiply(new BigDecimal("100")).divide(prev, 1, RoundingMode.HALF_UP);
    }

    private BigDecimal rate(Integer current, Integer previous) {
        return rate(BigDecimal.valueOf(count(current)), BigDecimal.valueOf(count(previous)));
    }

    private BigDecimal money(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private int count(Integer value) {
        return value == null ? 0 : value;
    }

    private <T> List<T> nvlList(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }

    private Date toDate(LocalDate day) {
        ZonedDateTime zdt = day.atStartOfDay(SHANGHAI);
        return Date.from(zdt.toInstant());
    }
}
