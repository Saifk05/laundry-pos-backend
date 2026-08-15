package com.laundry.pos.service;

import com.laundry.pos.model.Order;
import com.laundry.pos.model.Payment;

import com.laundry.pos.repository.OrderRepository;
import com.laundry.pos.repository.PaymentRepository;

import com.laundry.pos.response.BillListResponse;
import com.laundry.pos.response.BillResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class BillService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final BillPdfService billPdfService;
    private final WhatsAppService whatsAppService;

    public BillService(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            BillPdfService billPdfService,
            WhatsAppService whatsAppService
    ) {
        this.orderRepository =
                orderRepository;

        this.paymentRepository =
                paymentRepository;

        this.billPdfService =
                billPdfService;

        this.whatsAppService =
                whatsAppService;
    }

    @Transactional(readOnly = true)
    public BillListResponse getBills() {

        List<Order> orders =
                orderRepository
                        .findAllByOrderByCreatedAtDesc();

        List<BillResponse> bills =
                orders
                        .stream()
                        .map(this::toBillResponse)
                        .toList();

        BigDecimal totalPaidAmount =
                bills
                        .stream()
                        .map(BillResponse::paidAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalDueAmount =
                bills
                        .stream()
                        .map(BillResponse::dueAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalAmount =
                bills
                        .stream()
                        .map(BillResponse::total)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalTax =
                bills
                        .stream()
                        .map(BillResponse::tax)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalTaxableAmount =
                bills
                        .stream()
                        .map(BillResponse::taxableAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalExpressAmount =
                bills
                        .stream()
                        .map(BillResponse::expressAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalDiscountAmount =
                bills
                        .stream()
                        .map(BillResponse::discountAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalGrossAmount =
                bills
                        .stream()
                        .map(BillResponse::grossTotal)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return new BillListResponse(
                "Bills fetched successfully",
                bills.size(),

                money(totalPaidAmount),

                money(totalDueAmount),

                money(totalAmount),

                money(totalTax),

                money(totalTaxableAmount),

                money(totalExpressAmount),

                money(totalDiscountAmount),

                money(totalGrossAmount),

                bills
        );
    }

    @Transactional(readOnly = true)
    public String sendReceiptToWhatsApp(
            UUID orderId
    ) {

        Order order =
                orderRepository
                        .findById(
                                orderId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Order not found"
                                )
                        );

        String mobile =
                order.getCustomer()
                        .getPhone();

        if (
                mobile == null ||
                mobile.isBlank()
        ) {

            throw new RuntimeException(
                    "Customer mobile number is required"
            );
        }

        String orderNumber =
                order.getOrderNumber();

        byte[] pdf =
                billPdfService
                        .generateReceipt(
                                orderId
                        );

        return whatsAppService
                .sendReceipt(
                        mobile,
                        pdf,
                        orderNumber
                );
    }

    private BillResponse toBillResponse(
            Order order
    ) {

        BigDecimal subtotal =
                money(
                        order.getSubtotal()
                );

        BigDecimal discountAmount =
                money(
                        order.getDiscountAmount()
                );

        BigDecimal expressAmount =
                money(
                        order.getExpressChargeAmount()
                );

        BigDecimal grossTotal =
                money(
                        order.getTotalAmount()
                );

        BigDecimal paidAmount =
                money(
                        order.getPaidAmount()
                );

        BigDecimal dueAmount =
                money(
                        order.getBalanceAmount()
                );

        BigDecimal tax =
                money(
                        BigDecimal.ZERO
                );

        BigDecimal taxableAmount =
                subtotal;

        BigDecimal total =
                subtotal.subtract(
                        discountAmount
                );

        if (
                total.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            total =
                    BigDecimal.ZERO;
        }

        total =
                money(
                        total
                );

        BillResponse.BillStatus billStatus =
                getBillStatus(
                        order
                );

        LocalDateTime paidAt =
                getPaidAt(
                        order
                );

        String invoiceNumber =
                buildInvoiceNumber(
                        order.getOrderNumber()
                );

        return new BillResponse(
                order.getId(),

                invoiceNumber,

                order.getOrderNumber(),

                billStatus,

                paidAmount,

                dueAmount,

                total,

                tax,

                taxableAmount,

                expressAmount,

                discountAmount,

                grossTotal,

                order.getCreatedAt(),

                paidAt,

                order.getDeliveredAt(),

                order.getStatus()
        );
    }

    private BillResponse.BillStatus getBillStatus(
            Order order
    ) {

        if (
                order.getStatus()
                        == Order.OrderStatus.CANCELLED
        ) {

            return BillResponse.BillStatus.CANCELLED;
        }

        if (
                order.getPaymentStatus()
                        == Order.PaymentStatus.SETTLED
        ) {

            return BillResponse.BillStatus.PAID;
        }

        if (
                order.getPaymentStatus()
                        == Order.PaymentStatus.PARTIALLY_PAID
        ) {

            return BillResponse.BillStatus.PARTIALLY_PAID;
        }

        return BillResponse.BillStatus.DRAFT;
    }

    private LocalDateTime getPaidAt(
            Order order
    ) {

        if (
                order.getPaymentStatus()
                        != Order.PaymentStatus.SETTLED
        ) {

            return null;
        }

        List<Payment> payments =
                paymentRepository
                        .findAllByOrder_IdOrderByPaidAtDesc(
                                order.getId()
                        );

        return payments
                .stream()
                .map(Payment::getPaidAt)
                .filter(
                        paidAt ->
                                paidAt != null
                )
                .max(
                        Comparator.naturalOrder()
                )
                .orElse(null);
    }

    private String buildInvoiceNumber(
            String orderNumber
    ) {

        if (
                orderNumber == null ||
                orderNumber.isBlank()
        ) {

            return null;
        }

        if (
                orderNumber.startsWith(
                        "LAUNDRY-"
                )
        ) {

            return "INV-" +
                    orderNumber.substring(
                            "LAUNDRY-".length()
                    );
        }

        return "INV-" +
                orderNumber;
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