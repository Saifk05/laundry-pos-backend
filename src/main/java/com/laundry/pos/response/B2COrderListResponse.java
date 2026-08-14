package com.laundry.pos.response;

import java.util.List;

public record B2COrderListResponse(
        String message,
        long totalOrders,
        List<B2COrderResponse> orders
) {
}