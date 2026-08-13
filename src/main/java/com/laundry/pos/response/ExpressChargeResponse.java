package com.laundry.pos.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ExpressChargeResponse(
        UUID id,
        String name,
        BigDecimal percentage,
        boolean active
) {

    public record ExpressChargeListResponse(
            String message,
            List<ExpressChargeResponse> expressCharges
    ) {
    }
}