package com.laundry.pos.response;

import com.laundry.pos.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PaymentReportResponse(

        LocalDate fromDate,
        LocalDate toDate,

        BigDecimal totalAmount,
        BigDecimal cashAmount,
        BigDecimal upiAmount,
        BigDecimal cardAmount,
        BigDecimal otherAmount,

        List<PaymentDateResponse> dates

) {

    public record PaymentDateResponse(

            LocalDate date,

            BigDecimal totalAmount,
            BigDecimal cashAmount,
            BigDecimal upiAmount,
            BigDecimal cardAmount,
            BigDecimal otherAmount,

            List<PaymentOrderResponse> cashOrders,
            List<PaymentOrderResponse> upiOrders,
            List<PaymentOrderResponse> cardOrders,
            List<PaymentOrderResponse> otherOrders

    ) {
    }


    public record PaymentOrderResponse(

            UUID paymentId,

            UUID orderId,

            String orderNumber,

            String customerName,

            String mobile,

            BigDecimal amount,

            Payment.PaymentMethod paymentMethod,

            String referenceNumber,

            LocalDateTime paidAt

    ) {
    }
}