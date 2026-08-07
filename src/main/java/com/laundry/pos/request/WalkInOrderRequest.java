package com.laundry.pos.request;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record WalkInOrderRequest(
        String customerName,
        String customerPhone,
        LocalDate deliveryDate,
        String deliverySlot,
        boolean homeDelivery,
        boolean expressDelivery,
        boolean washingArea,
        boolean pressingArea,
        String couponCode,
        List<Item> items
) {

    public record Item(
            UUID productId,
            UUID serviceId,
            Integer quantity
    ) {
    }
}