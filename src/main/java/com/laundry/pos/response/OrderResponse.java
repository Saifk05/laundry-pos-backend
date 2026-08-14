package com.laundry.pos.response;

import com.laundry.pos.model.Order;
import com.laundry.pos.model.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        CustomerResponse customer,
        List<OrderItemResponse> items,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        String couponCode,
        BigDecimal expressChargePercentage,
        BigDecimal expressChargeAmount,
        BigDecimal totalAmount,
        Order.OrderStatus status,
        LocalDateTime createdAt,
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