package com.laundry.pos.response;

import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        String customerName,
        String customerPhone,
        Integer totalPieces,
        Integer subtotal,
        Integer couponDiscount,
        Integer expressCharge,
        Integer totalAmount,
        String status,
        String message
) {
}