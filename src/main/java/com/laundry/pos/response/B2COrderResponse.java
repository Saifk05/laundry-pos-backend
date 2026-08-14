package com.laundry.pos.response;

import com.laundry.pos.model.Order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record B2COrderResponse(
        UUID id,
        String orderNumber,
        String customerName,
        String mobile,
        BigDecimal totalAmount,
        LocalDate pickupDate,
        String pickupTime,
        LocalDate deliveryDate,
        String deliveryTime,
        String storageLabel,
        boolean homeDelivery,
        boolean settled,
        Order.OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}