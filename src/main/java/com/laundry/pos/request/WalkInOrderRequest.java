package com.laundry.pos.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record WalkInOrderRequest(
        CustomerRequest customer,
        List<OrderItemRequest> items,
        UUID couponId,
        UUID expressChargeId,
        LocalDate deliveryDate,
        String deliveryTime,
        Boolean homeDelivery
) {

    public record CustomerRequest(
            String name,
            String phone
    ) {
    }

    public record OrderItemRequest(
            UUID productId,
            UUID typeId,
            UUID serviceId,
            BigDecimal quantity
    ) {
    }
}