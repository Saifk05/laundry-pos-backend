package com.laundry.pos.request;

import com.laundry.pos.model.Coupon;

import java.math.BigDecimal;

public record CouponRequest(
        String code,
        Coupon.DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minimumOrderAmount,
        boolean active
) {
}