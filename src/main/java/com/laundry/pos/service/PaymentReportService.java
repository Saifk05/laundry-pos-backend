package com.laundry.pos.service;

import com.laundry.pos.model.Payment;

import com.laundry.pos.repository.PaymentRepository;

import com.laundry.pos.response.PaymentReportResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentReportService {

    private final PaymentRepository paymentRepository;

    public PaymentReportService(
            PaymentRepository paymentRepository
    ) {

        this.paymentRepository =
                paymentRepository;
    }


    @Transactional(readOnly = true)
    public PaymentReportResponse getPaymentReport(
            LocalDate fromDate,
            LocalDate toDate
    ) {

        if (fromDate == null) {
            throw new RuntimeException(
                    "From date is required"
            );
        }

        if (toDate == null) {
            throw new RuntimeException(
                    "To date is required"
            );
        }

        if (
                fromDate.isAfter(
                        toDate
                )
        ) {

            throw new RuntimeException(
                    "From date cannot be after to date"
            );
        }


        LocalDateTime fromDateTime =
                fromDate.atStartOfDay();

        LocalDateTime toDateTime =
                toDate.atTime(
                        LocalTime.MAX
                );


        List<Payment> payments =
                paymentRepository
                        .findAllByPaidAtBetweenOrderByPaidAtDesc(
                                fromDateTime,
                                toDateTime
                        );


        BigDecimal totalAmount =
                sumPayments(
                        payments
                );

        BigDecimal cashAmount =
                sumPaymentsByMethod(
                        payments,
                        Payment.PaymentMethod.CASH
                );

        BigDecimal upiAmount =
                sumPaymentsByMethod(
                        payments,
                        Payment.PaymentMethod.UPI
                );

        BigDecimal cardAmount =
                sumPaymentsByMethod(
                        payments,
                        Payment.PaymentMethod.CARD
                );

        BigDecimal otherAmount =
                sumPaymentsByMethod(
                        payments,
                        Payment.PaymentMethod.OTHER
                );


        Map<LocalDate, List<Payment>>
                groupedPayments =
                new LinkedHashMap<>();


        payments
                .stream()
                .sorted(
                        Comparator.comparing(
                                Payment::getPaidAt
                        ).reversed()
                )
                .forEach(payment -> {

                    LocalDate paymentDate =
                            payment.getPaidAt()
                                    .toLocalDate();

                    groupedPayments
                            .computeIfAbsent(
                                    paymentDate,
                                    key ->
                                            new ArrayList<>()
                            )
                            .add(
                                    payment
                            );
                });


        List<PaymentReportResponse.PaymentDateResponse>
                dates =
                groupedPayments
                        .entrySet()
                        .stream()
                        .map(entry ->
                                toDateResponse(
                                        entry.getKey(),
                                        entry.getValue()
                                )
                        )
                        .toList();


        return new PaymentReportResponse(
                fromDate,
                toDate,

                totalAmount,
                cashAmount,
                upiAmount,
                cardAmount,
                otherAmount,

                dates
        );
    }


    private PaymentReportResponse.PaymentDateResponse
    toDateResponse(
            LocalDate date,
            List<Payment> payments
    ) {

        List<Payment> cashPayments =
                filterByMethod(
                        payments,
                        Payment.PaymentMethod.CASH
                );

        List<Payment> upiPayments =
                filterByMethod(
                        payments,
                        Payment.PaymentMethod.UPI
                );

        List<Payment> cardPayments =
                filterByMethod(
                        payments,
                        Payment.PaymentMethod.CARD
                );

        List<Payment> otherPayments =
                filterByMethod(
                        payments,
                        Payment.PaymentMethod.OTHER
                );


        return new PaymentReportResponse.PaymentDateResponse(
                date,

                sumPayments(
                        payments
                ),

                sumPayments(
                        cashPayments
                ),

                sumPayments(
                        upiPayments
                ),

                sumPayments(
                        cardPayments
                ),

                sumPayments(
                        otherPayments
                ),

                cashPayments
                        .stream()
                        .map(this::toPaymentOrderResponse)
                        .toList(),

                upiPayments
                        .stream()
                        .map(this::toPaymentOrderResponse)
                        .toList(),

                cardPayments
                        .stream()
                        .map(this::toPaymentOrderResponse)
                        .toList(),

                otherPayments
                        .stream()
                        .map(this::toPaymentOrderResponse)
                        .toList()
        );
    }


    private List<Payment> filterByMethod(
            List<Payment> payments,
            Payment.PaymentMethod paymentMethod
    ) {

        return payments
                .stream()
                .filter(payment ->
                        payment.getPaymentMethod()
                                == paymentMethod
                )
                .toList();
    }


    private BigDecimal sumPaymentsByMethod(
            List<Payment> payments,
            Payment.PaymentMethod paymentMethod
    ) {

        return payments
                .stream()
                .filter(payment ->
                        payment.getPaymentMethod()
                                == paymentMethod
                )
                .map(
                        Payment::getAmount
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }


    private BigDecimal sumPayments(
            List<Payment> payments
    ) {

        return payments
                .stream()
                .map(
                        Payment::getAmount
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }


    private PaymentReportResponse.PaymentOrderResponse
    toPaymentOrderResponse(
            Payment payment
    ) {

        return new PaymentReportResponse.PaymentOrderResponse(
                payment.getId(),

                payment.getOrder()
                        .getId(),

                payment.getOrder()
                        .getOrderNumber(),

                payment.getOrder()
                        .getCustomer()
                        .getName(),

                payment.getOrder()
                        .getCustomer()
                        .getPhone(),

                payment.getAmount(),

                payment.getPaymentMethod(),

                payment.getReferenceNumber(),

                payment.getPaidAt()
        );
    }
}