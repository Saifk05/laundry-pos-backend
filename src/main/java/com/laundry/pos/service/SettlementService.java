package com.laundry.pos.service;

import com.laundry.pos.model.Order;
import com.laundry.pos.model.Payment;

import com.laundry.pos.repository.OrderRepository;
import com.laundry.pos.repository.PaymentRepository;

import com.laundry.pos.request.PaymentRequest;

import com.laundry.pos.response.PaymentHistoryResponse;
import com.laundry.pos.response.SettlementResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class SettlementService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public SettlementService(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository
    ) {

        this.orderRepository =
                orderRepository;

        this.paymentRepository =
                paymentRepository;
    }

    @Transactional(readOnly = true)
    public List<SettlementResponse> getSettlements() {

        return orderRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toSettlementResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SettlementResponse getSettlement(
            UUID orderId
    ) {

        Order order =
                getOrder(
                        orderId
                );

        return toSettlementResponse(
                order
        );
    }

    @Transactional
    public SettlementResponse addPayment(
            UUID orderId,
            PaymentRequest request
    ) {

        validatePaymentRequest(
                request
        );

        Order order =
                getOrder(
                        orderId
                );

        if (
                order.getStatus()
                        == Order.OrderStatus.CANCELLED
        ) {

            throw new RuntimeException(
                    "Payment cannot be added to cancelled order"
            );
        }

        BigDecimal currentBalance =
                money(
                        order.getBalanceAmount()
                );

        if (
                currentBalance.compareTo(
                        BigDecimal.ZERO
                ) <= 0
        ) {

            throw new RuntimeException(
                    "Order is already fully settled"
            );
        }

        BigDecimal paymentAmount =
                money(
                        request.amount()
                );

        if (
                paymentAmount.compareTo(
                        currentBalance
                ) > 0
        ) {

            throw new RuntimeException(
                    "Payment amount cannot be greater than balance amount"
            );
        }

        Payment payment =
                new Payment();

        payment.setOrder(
                order
        );

        payment.setAmount(
                paymentAmount
        );

        payment.setPaymentMethod(
                request.paymentMethod()
        );

        if (
                request.referenceNumber() != null &&
                !request.referenceNumber()
                        .isBlank()
        ) {

            payment.setReferenceNumber(
                    request.referenceNumber()
                            .trim()
            );
        }

        paymentRepository.save(
                payment
        );

        BigDecimal currentPaidAmount =
                money(
                        order.getPaidAmount()
                );

        BigDecimal newPaidAmount =
                currentPaidAmount.add(
                        paymentAmount
                );

        newPaidAmount =
                money(
                        newPaidAmount
                );

        BigDecimal newBalanceAmount =
                order.getTotalAmount()
                        .subtract(
                                newPaidAmount
                        );

        if (
                newBalanceAmount.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {

            newBalanceAmount =
                    BigDecimal.ZERO;
        }

        newBalanceAmount =
                money(
                        newBalanceAmount
                );

        order.setPaidAmount(
                newPaidAmount
        );

        order.setBalanceAmount(
                newBalanceAmount
        );

        if (
                newBalanceAmount.compareTo(
                        BigDecimal.ZERO
                ) == 0
        ) {

            order.setPaymentStatus(
                    Order.PaymentStatus.SETTLED
            );

            order.setSettled(
                    true
            );

        } else {

            order.setPaymentStatus(
                    Order.PaymentStatus.PARTIALLY_PAID
            );

            order.setSettled(
                    false
            );
        }

        Order updatedOrder =
                orderRepository.save(
                        order
                );

        return toSettlementResponse(
                updatedOrder
        );
    }

    @Transactional(readOnly = true)
    public PaymentHistoryResponse getPaymentHistory(
            UUID orderId
    ) {

        Order order =
                getOrder(
                        orderId
                );

        List<PaymentHistoryResponse.PaymentResponse>
                payments =
                paymentRepository
                        .findAllByOrder_IdOrderByPaidAtDesc(
                                orderId
                        )
                        .stream()
                        .map(payment ->
                                new PaymentHistoryResponse.PaymentResponse(
                                        payment.getId(),
                                        payment.getAmount(),
                                        payment.getPaymentMethod(),
                                        payment.getReferenceNumber(),
                                        payment.getPaidAt()
                                )
                        )
                        .toList();

        return new PaymentHistoryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer()
                        .getName(),
                order.getCustomer()
                        .getPhone(),
                money(
                        order.getTotalAmount()
                ),
                money(
                        order.getPaidAmount()
                ),
                money(
                        order.getBalanceAmount()
                ),
                order.getPaymentStatus(),
                payments
        );
    }

    private Order getOrder(
            UUID orderId
    ) {

        return orderRepository
                .findById(
                        orderId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found"
                        )
                );
    }

    private void validatePaymentRequest(
            PaymentRequest request
    ) {

        if (
                request == null
        ) {

            throw new RuntimeException(
                    "Payment request is required"
            );
        }

        if (
                request.amount() == null
        ) {

            throw new RuntimeException(
                    "Payment amount is required"
            );
        }

        if (
                request.amount()
                        .compareTo(
                                BigDecimal.ZERO
                        ) <= 0
        ) {

            throw new RuntimeException(
                    "Payment amount must be greater than 0"
            );
        }

        if (
                request.paymentMethod() == null
        ) {

            throw new RuntimeException(
                    "Payment method is required"
            );
        }
    }

    private SettlementResponse toSettlementResponse(
            Order order
    ) {

        return new SettlementResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer()
                        .getName(),
                order.getCustomer()
                        .getPhone(),
                money(
                        order.getTotalAmount()
                ),
                money(
                        order.getPaidAmount()
                ),
                money(
                        order.getBalanceAmount()
                ),
                order.getPaymentStatus(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private BigDecimal money(
            BigDecimal value
    ) {

        if (
                value == null
        ) {

            return BigDecimal.ZERO
                    .setScale(
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }
}