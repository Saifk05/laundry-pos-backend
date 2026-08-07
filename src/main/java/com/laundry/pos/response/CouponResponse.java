package com.laundry.pos.response;

import java.util.UUID;

public record CouponResponse(
        UUID id,
        String code,
        Integer amount,
        boolean active
) {
}