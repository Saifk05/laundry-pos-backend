package com.laundry.pos.response;

import com.laundry.pos.model.Order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DashboardResponse(
        int totalOrders,
        int processingOrders,
        int readyOrders,
        List<DeliveryDateResponse> dates
) {

    public record DeliveryDateResponse(
            LocalDate deliveryDate,
            String dayLabel,

            int totalOrders,
            BigDecimal totalPieces,

            int processingOrders,
            BigDecimal processingPieces,

            int readyOrders,
            BigDecimal readyPieces,

            List<DashboardOrderResponse> orders
    ) {
    }


    public record DashboardOrderResponse(
            UUID id,
            String orderNumber,
            String customerName,
            String mobile,
            BigDecimal totalAmount,
            BigDecimal totalPieces,
            String deliveryTime,
            Order.OrderStatus status
    ) {
    }
}