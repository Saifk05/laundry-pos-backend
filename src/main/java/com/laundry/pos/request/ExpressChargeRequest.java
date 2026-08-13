package com.laundry.pos.request;

import java.math.BigDecimal;

public record ExpressChargeRequest(
        String name,
        BigDecimal percentage,
        boolean active
) {
}