package com.laundry.pos.response;

import com.laundry.pos.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BillResponse(
        UUID orderId,

        String invoiceNumber,

        String orderNumber,

        BillStatus status,

        BigDecimal paidAmount,

        BigDecimal dueAmount,

        BigDecimal total,

        BigDecimal tax,

        BigDecimal taxableAmount,

        BigDecimal expressAmount,

        BigDecimal discountAmount,

        BigDecimal grossTotal,

        LocalDateTime createdAt,

        LocalDateTime paidAt,

        LocalDateTime deliveredAt,

        Order.OrderStatus orderStatus
) {

    public enum BillStatus {

        DRAFT,

        PARTIALLY_PAID,

        PAID,

        CANCELLED
    }
}