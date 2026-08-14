package com.laundry.pos.request;

import java.time.LocalDate;

public record RescheduleOrderRequest(
        LocalDate deliveryDate,
        String deliveryTime
) {
}