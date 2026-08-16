package com.laundry.pos.response;

import com.laundry.pos.model.Order;
import com.laundry.pos.model.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        CustomerResponse customer,
        List<OrderItemResponse> items,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        String couponCode,
        boolean expressDelivery,
        BigDecimal expressChargePercentage,
        BigDecimal expressChargeAmount,
        BigDecimal totalAmount,

        BigDecimal paidAmount,
        BigDecimal balanceAmount,
        Order.PaymentStatus paymentStatus,

        LocalDate pickupDate,
        String pickupTime,
        LocalDate deliveryDate,
        String deliveryTime,
        String storageLabel,
        boolean homeDelivery,
        boolean settled,
        Order.OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String message
) {

    public record CustomerResponse(
            UUID id,
            String name,
            String phone
    ) {
    }

    public record OrderItemResponse(
            UUID id,
            UUID productId,
            String productName,
            UUID typeId,
            String typeName,
            UUID serviceId,
            String serviceName,
            Product.PricingUnit unit,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {
    }
}