package com.laundry.pos.request;

import com.laundry.pos.model.Payment;

import java.math.BigDecimal;

public record PaymentRequest(
        BigDecimal amount,
        Payment.PaymentMethod paymentMethod,
        String referenceNumber
) {
}