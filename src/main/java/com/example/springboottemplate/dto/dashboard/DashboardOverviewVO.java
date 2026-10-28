package com.example.springboottemplate.dto.dashboard;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "首页经营概览")
public class DashboardOverviewVO {
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty("当前店铺（商家）")
    private Long storeId;
    private String storeName;
    private Integer identityType;
    private String today;

    private BigDecimal todaySales;
    private BigDecimal yesterdaySales;
    private BigDecimal todaySalesRate;
    private Integer todayOrderCount;
    private Integer yesterdayOrderCount;
    private BigDecimal todayOrderRate;

    private BigDecimal monthSales;
    private BigDecimal lastMonthSales;
    private BigDecimal monthSalesRate;
    private Integer monthOrderCount;

    private Integer todayCustomers;
    private Integer yesterdayCustomers;
    private BigDecimal todayCustomerRate;
    private Integer todayNewCustomers;
    private Integer yesterdayNewCustomers;
    private BigDecimal todayNewCustomerRate;

    private BigDecimal todayRecharge;
    private BigDecimal yesterdayRecharge;
    private BigDecimal todayRechargeRate;
    private BigDecimal todayRefund;

    @Builder.Default
    private List<DashboardDayRow> trend = new ArrayList<>();
    @Builder.Default
    private List<DashboardHourRow> hourSales = new ArrayList<>();
    @Builder.Default
    private List<DashboardChannelRow> payChannels = new ArrayList<>();
    @Builder.Default
    private List<DashboardStoreRow> topStores = new ArrayList<>();
}
