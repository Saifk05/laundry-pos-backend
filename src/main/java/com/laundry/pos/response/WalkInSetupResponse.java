package com.laundry.pos.response;

import java.util.List;

public record WalkInSetupResponse(
        String message,
        List<ProductResponse> products,
        List<CouponResponse> coupons,
        List<ExpressChargeResponse> expressCharges
) {
}