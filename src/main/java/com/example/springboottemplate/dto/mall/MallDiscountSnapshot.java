package com.example.springboottemplate.dto.mall;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MallDiscountSnapshot {
    private Long promoId;
    private BigDecimal promoDiscount;
    private Long userCouponId;
    private BigDecimal couponDiscount;
    private BigDecimal discountAmount;
    private String discountDesc;
    private BigDecimal payAmount;
    private int payAmountFen;
}
