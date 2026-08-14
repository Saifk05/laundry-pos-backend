package com.laundry.pos.request;

import com.laundry.pos.model.Order;

public record OrderStatusRequest(
        Order.OrderStatus status
) {
}