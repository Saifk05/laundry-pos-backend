package com.laundry.pos.response;

import com.laundry.pos.model.Order;
import com.laundry.pos.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PaymentHistoryResponse(
        UUID orderId,
        String orderNumber,
        String customerName,
        String mobile,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal balanceAmount,
        Order.PaymentStatus paymentStatus,
        List<PaymentResponse> payments
) {

    public record PaymentResponse(
            UUID id,
            BigDecimal amount,
            Payment.PaymentMethod paymentMethod,
            String referenceNumber,
            LocalDateTime paidAt
    ) {
    }
}