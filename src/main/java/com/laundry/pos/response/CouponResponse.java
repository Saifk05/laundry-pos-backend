package com.laundry.pos.response;

import com.laundry.pos.model.Coupon;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CouponResponse(
        UUID id,
        String code,
        Coupon.DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minimumOrderAmount,
        boolean active
) {

    public record CouponListResponse(
            String message,
            List<CouponResponse> coupons
    ) {
    }
}