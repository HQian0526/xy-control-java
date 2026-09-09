package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.MallOrder;
import com.example.springboottemplate.dto.mall.MallIncomeFlowRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MallOrderMapper extends BaseMapper<MallOrder> {
    void addMallOrder(MallOrder order);

    MallOrder selectByOrderNo(@Param("orderNo") String orderNo);

    java.util.List<MallOrder> findMallOrder(MallOrder order);

    int markPaid(@Param("orderNo") String orderNo,
                 @Param("transactionId") String transactionId,
                 @Param("paidTime") java.util.Date paidTime);

    int updatePrepayId(@Param("orderNo") String orderNo, @Param("prepayId") String prepayId);

    int updateWxShippingSync(@Param("orderNo") String orderNo,
                             @Param("status") Integer status,
                             @Param("errcode") Integer errcode,
                             @Param("errmsg") String errmsg,
                             @Param("retry") Integer retry,
                             @Param("shippingTime") java.util.Date shippingTime);

    int markClosed(@Param("orderNo") String orderNo,
                   @Param("closedTime") java.util.Date closedTime);

    java.util.List<MallOrder> selectExpiredUnpaid(@Param("expireBefore") java.util.Date expireBefore,
                                                  @Param("limit") int limit);

    java.util.List<MallOrder> selectPendingWxShipping(@Param("maxRetry") int maxRetry,
                                                      @Param("limit") int limit);

    int markRefunding(@Param("orderNo") String orderNo,
                      @Param("pendingRefundFen") Integer pendingRefundFen,
                      @Param("outRefundNo") String outRefundNo,
                      @Param("reason") String reason,
                      @Param("refundBy") String refundBy);

    int markRefundSuccess(@Param("orderNo") String orderNo,
                          @Param("refundFen") Integer refundFen,
                          @Param("outRefundNo") String outRefundNo,
                          @Param("wxRefundId") String wxRefundId,
                          @Param("refundTime") java.util.Date refundTime,
                          @Param("allowDirect") int allowDirect);

    int markRefundFailed(@Param("orderNo") String orderNo,
                         @Param("outRefundNo") String outRefundNo);

    MallOrder selectByOutRefundNo(@Param("outRefundNo") String outRefundNo);

    java.util.List<MallIncomeFlowRow> sumIncomeFlow(@Param("storeId") Long storeId,
                                                    @Param("periodType") String periodType,
                                                    @Param("startTime") java.util.Date startTime,
                                                    @Param("endTime") java.util.Date endTime);

    java.util.List<com.example.springboottemplate.dto.mall.MallFinanceRecord> selectFinanceLedger(
            @Param("storeId") Long storeId,
            @Param("type") String type,
            @Param("startTime") java.util.Date startTime,
            @Param("endTime") java.util.Date endTime);

    com.example.springboottemplate.dto.mall.MallFinanceSummary sumFinanceLedger(
            @Param("storeId") Long storeId,
            @Param("startTime") java.util.Date startTime,
            @Param("endTime") java.util.Date endTime);
}
