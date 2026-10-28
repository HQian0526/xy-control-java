package com.example.springboottemplate.mapper;

import com.example.springboottemplate.dto.dashboard.DashboardChannelRow;
import com.example.springboottemplate.dto.dashboard.DashboardDayRow;
import com.example.springboottemplate.dto.dashboard.DashboardHourRow;
import com.example.springboottemplate.dto.dashboard.DashboardStoreRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Mapper
public interface DashboardMapper {
    BigDecimal sumNetSales(@Param("storeId") Long storeId,
                           @Param("startTime") Date startTime,
                           @Param("endTime") Date endTime);

    Integer countPaidOrders(@Param("storeId") Long storeId,
                            @Param("startTime") Date startTime,
                            @Param("endTime") Date endTime);

    BigDecimal sumRefund(@Param("storeId") Long storeId,
                         @Param("startTime") Date startTime,
                         @Param("endTime") Date endTime);

    BigDecimal sumRecharge(@Param("storeId") Long storeId,
                           @Param("startTime") Date startTime,
                           @Param("endTime") Date endTime);

    Integer countCustomers(@Param("storeId") Long storeId,
                           @Param("startTime") Date startTime,
                           @Param("endTime") Date endTime);

    Integer countNewCustomers(@Param("storeId") Long storeId,
                              @Param("startTime") Date startTime,
                              @Param("endTime") Date endTime);

    List<DashboardDayRow> listSalesTrend(@Param("storeId") Long storeId,
                                         @Param("startTime") Date startTime,
                                         @Param("endTime") Date endTime);

    List<DashboardDayRow> listCustomerTrend(@Param("storeId") Long storeId,
                                            @Param("startTime") Date startTime,
                                            @Param("endTime") Date endTime);

    List<DashboardDayRow> listNewCustomerTrend(@Param("storeId") Long storeId,
                                               @Param("startTime") Date startTime,
                                               @Param("endTime") Date endTime);

    List<DashboardHourRow> listHourSales(@Param("storeId") Long storeId,
                                         @Param("startTime") Date startTime,
                                         @Param("endTime") Date endTime);

    List<DashboardChannelRow> listPayChannels(@Param("storeId") Long storeId,
                                              @Param("startTime") Date startTime,
                                              @Param("endTime") Date endTime);

    List<DashboardStoreRow> listTopStores(@Param("startTime") Date startTime,
                                          @Param("endTime") Date endTime,
                                          @Param("limit") int limit);
}
