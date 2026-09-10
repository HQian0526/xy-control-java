package com.example.springboottemplate.utils;

import com.example.springboottemplate.entity.MallPromo;
import com.example.springboottemplate.entity.MallPromoTier;
import com.example.springboottemplate.entity.MallUserCoupon;
import com.example.springboottemplate.dto.mall.MallDiscountSnapshot;
import com.example.springboottemplate.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class MallDiscountCalculator {

    private MallDiscountCalculator() {
    }

    public static boolean isEffective(Integer forever, Date endTime, Date now) {
        Date at = now == null ? new Date() : now;
        if (forever == null || forever == 1) {
            return true;
        }
        return endTime != null && endTime.after(at);
    }

    public static String expireText(Integer forever, Date endTime) {
        if (forever == null || forever == 1) {
            return "永久有效";
        }
        if (endTime == null) {
            return "未设置过期时间";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endTime);
    }

    public static String tierText(List<MallPromoTier> tiers) {
        if (tiers == null || tiers.isEmpty()) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (MallPromoTier tier : tiers) {
            if (tier == null || tier.getThresholdAmount() == null || tier.getDiscountAmount() == null) {
                continue;
            }
            parts.add("满" + strip(tier.getThresholdAmount()) + "减" + strip(tier.getDiscountAmount()));
        }
        return String.join("；", parts);
    }

    public static BigDecimal pickPromoDiscount(MallPromo promo, BigDecimal goodsAmount) {
        if (promo == null || promo.getTiers() == null || goodsAmount == null) {
            return BigDecimal.ZERO;
        }
        MallPromoTier best = null;
        for (MallPromoTier tier : promo.getTiers()) {
            if (tier == null || tier.getThresholdAmount() == null || tier.getDiscountAmount() == null) {
                continue;
            }
            if (goodsAmount.compareTo(tier.getThresholdAmount()) < 0) {
                continue;
            }
            if (best == null) {
                best = tier;
                continue;
            }
            int cmp = tier.getDiscountAmount().compareTo(best.getDiscountAmount());
            if (cmp > 0 || (cmp == 0 && tier.getThresholdAmount().compareTo(best.getThresholdAmount()) > 0)) {
                best = tier;
            }
        }
        return best == null ? BigDecimal.ZERO : best.getDiscountAmount();
    }

    public static MallDiscountSnapshot compute(BigDecimal goodsAmount,
                                              BigDecimal deliveryFee,
                                              MallPromo promo,
                                              MallUserCoupon coupon) {
        BigDecimal goods = nvl(goodsAmount);
        BigDecimal fee = nvl(deliveryFee);
        BigDecimal promoDiscount = BigDecimal.ZERO;
        Long promoId = null;
        String promoDesc = null;
        if (promo != null && isEffective(promo.getForever(), promo.getEndTime(), new Date())) {
            promoDiscount = pickPromoDiscount(promo, goods).setScale(2, RoundingMode.HALF_UP);
            if (promoDiscount.compareTo(BigDecimal.ZERO) > 0) {
                promoId = promo.getId();
                MallPromoTier hit = hitTier(promo, goods);
                promoDesc = hit == null
                        ? ("满减减" + strip(promoDiscount))
                        : ("满" + strip(hit.getThresholdAmount()) + "减" + strip(hit.getDiscountAmount()));
            } else {
                promoDiscount = BigDecimal.ZERO;
            }
        }

        BigDecimal afterFull = goods.subtract(promoDiscount).max(BigDecimal.ZERO);
        BigDecimal couponDiscount = BigDecimal.ZERO;
        Long userCouponId = null;
        String couponDesc = null;
        if (coupon != null) {
            BigDecimal threshold = nvl(coupon.getThresholdAmount());
            if (goods.compareTo(threshold) < 0) {
                throw new BusinessException("未达到优惠券使用门槛");
            }
            couponDiscount = nvl(coupon.getDiscountAmount()).min(afterFull).setScale(2, RoundingMode.HALF_UP);
            if (couponDiscount.compareTo(BigDecimal.ZERO) > 0) {
                userCouponId = coupon.getId();
                couponDesc = "优惠券减" + strip(couponDiscount);
            } else {
                couponDiscount = BigDecimal.ZERO;
            }
        }

        BigDecimal discountAmount = promoDiscount.add(couponDiscount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal payAmount = afterFull.subtract(couponDiscount).add(fee).setScale(2, RoundingMode.HALF_UP);
        int payAmountFen = payAmount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValueExact();
        if (payAmountFen < 1) {
            throw new BusinessException("优惠后应付金额必须大于0");
        }

        List<String> desc = new ArrayList<>();
        if (promoDesc != null) {
            desc.add(promoDesc);
        }
        if (couponDesc != null) {
            desc.add(couponDesc);
        }
        return MallDiscountSnapshot.builder()
                .promoId(promoId)
                .promoDiscount(promoDiscount)
                .userCouponId(userCouponId)
                .couponDiscount(couponDiscount)
                .discountAmount(discountAmount)
                .discountDesc(desc.isEmpty() ? null : String.join("；", desc))
                .payAmount(payAmount)
                .payAmountFen(payAmountFen)
                .build();
    }

    public static MallDiscountSnapshot tryCompute(BigDecimal goodsAmount,
                                                 BigDecimal deliveryFee,
                                                 MallPromo promo,
                                                 MallUserCoupon coupon) {
        try {
            return compute(goodsAmount, deliveryFee, promo, coupon);
        } catch (BusinessException e) {
            return null;
        }
    }

    private static MallPromoTier hitTier(MallPromo promo, BigDecimal goodsAmount) {
        MallPromoTier best = null;
        for (MallPromoTier tier : promo.getTiers()) {
            if (tier == null || tier.getThresholdAmount() == null || tier.getDiscountAmount() == null) {
                continue;
            }
            if (goodsAmount.compareTo(tier.getThresholdAmount()) < 0) {
                continue;
            }
            if (best == null) {
                best = tier;
                continue;
            }
            int cmp = tier.getDiscountAmount().compareTo(best.getDiscountAmount());
            if (cmp > 0 || (cmp == 0 && tier.getThresholdAmount().compareTo(best.getThresholdAmount()) > 0)) {
                best = tier;
            }
        }
        return best;
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String strip(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
