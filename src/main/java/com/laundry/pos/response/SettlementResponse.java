package com.laundry.pos.response;

import com.laundry.pos.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SettlementResponse(
        UUID id,
        String orderNumber,
        String customerName,
        String mobile,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal balanceAmount,
        Order.PaymentStatus paymentStatus,
        Order.OrderStatus orderStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}