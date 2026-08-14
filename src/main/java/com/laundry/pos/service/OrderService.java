package com.laundry.pos.service;

import com.laundry.pos.model.Order;
import com.laundry.pos.repository.OrderRepository;
import com.laundry.pos.request.OrderStatusRequest;
import com.laundry.pos.request.RescheduleOrderRequest;
import com.laundry.pos.response.B2COrderListResponse;
import com.laundry.pos.response.B2COrderResponse;
import com.laundry.pos.response.OrderResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(
            OrderRepository orderRepository
    ) {
        this.orderRepository =
                orderRepository;
    }

    @Transactional(readOnly = true)
    public B2COrderListResponse getOrders(
            Order.OrderStatus status,
            String search
    ) {

        String normalizedSearch =
                search == null
                        ? ""
                        : search.trim();

        List<Order> orders =
                orderRepository.searchOrders(
                        status,
                        normalizedSearch
                );

        List<B2COrderResponse> responses =
                orders
                        .stream()
                        .map(this::toB2COrderResponse)
                        .toList();

        String message =
                responses.isEmpty()
                        ? "No orders found"
                        : "Orders fetched successfully";

        return new B2COrderListResponse(
                message,
                responses.size(),
                responses
        );
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(
            UUID id
    ) {

        Order order =
                getOrder(
                        id
                );

        return toOrderResponse(
                order,
                "Order fetched successfully"
        );
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(
            String orderNumber
    ) {

        if (
                orderNumber == null ||
                orderNumber.isBlank()
        ) {

            throw new RuntimeException(
                    "Order number is required"
            );
        }

        Order order =
                orderRepository
                        .findByOrderNumber(
                                orderNumber
                                        .trim()
                                        .toUpperCase()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Order not found"
                                )
                        );

        return toOrderResponse(
                order,
                "Order fetched successfully"
        );
    }

    @Transactional
    public B2COrderResponse updateStatus(
            UUID id,
            OrderStatusRequest request
    ) {

        if (
                request == null ||
                request.status() == null
        ) {

            throw new RuntimeException(
                    "Order status is required"
            );
        }

        Order order =
                getOrder(
                        id
                );

        validateStatusChange(
                order,
                request.status()
        );

        order.setStatus(
                request.status()
        );

        Order updatedOrder =
                orderRepository.save(
                        order
                );

        return toB2COrderResponse(
                updatedOrder
        );
    }

    @Transactional
    public B2COrderResponse markReady(
            UUID id
    ) {

        Order order =
                getOrder(
                        id
                );

        if (
                order.getStatus()
                        != Order.OrderStatus
                        .PROCESSING_AT_STORE
        ) {

            throw new RuntimeException(
                    "Only processing orders can be marked ready"
            );
        }

        order.setStatus(
                Order.OrderStatus.READY_ORDER
        );

        Order updatedOrder =
                orderRepository.save(
                        order
                );

        return toB2COrderResponse(
                updatedOrder
        );
    }

    @Transactional
    public B2COrderResponse markDelivered(
            UUID id
    ) {

        Order order =
                getOrder(
                        id
                );

        if (
                order.getStatus()
                        != Order.OrderStatus.READY_ORDER
        ) {

            throw new RuntimeException(
                    "Only ready orders can be marked delivered"
            );
        }

        order.setStatus(
                Order.OrderStatus.DELIVERED
        );

        Order updatedOrder =
                orderRepository.save(
                        order
                );

        return toB2COrderResponse(
                updatedOrder
        );
    }

    @Transactional
    public B2COrderResponse cancelOrder(
            UUID id
    ) {

        Order order =
                getOrder(
                        id
                );

        if (
                order.getStatus()
                        == Order.OrderStatus.DELIVERED
        ) {

            throw new RuntimeException(
                    "Delivered order cannot be cancelled"
            );
        }

        if (
                order.getStatus()
                        == Order.OrderStatus.CANCELLED
        ) {

            throw new RuntimeException(
                    "Order is already cancelled"
            );
        }

        order.setStatus(
                Order.OrderStatus.CANCELLED
        );

        Order updatedOrder =
                orderRepository.save(
                        order
                );

        return toB2COrderResponse(
                updatedOrder
        );
    }

    @Transactional
    public B2COrderResponse rescheduleOrder(
            UUID id,
            RescheduleOrderRequest request
    ) {

        if (
                request == null
        ) {

            throw new RuntimeException(
                    "Reschedule request is required"
            );
        }

        if (
                request.deliveryDate() == null
        ) {

            throw new RuntimeException(
                    "Delivery date is required"
            );
        }

        if (
                request.deliveryTime() == null ||
                request.deliveryTime().isBlank()
        ) {

            throw new RuntimeException(
                    "Delivery time is required"
            );
        }

        Order order =
                getOrder(
                        id
                );

        if (
                order.getStatus()
                        == Order.OrderStatus.DELIVERED
        ) {

            throw new RuntimeException(
                    "Delivered order cannot be rescheduled"
            );
        }

        if (
                order.getStatus()
                        == Order.OrderStatus.CANCELLED
        ) {

            throw new RuntimeException(
                    "Cancelled order cannot be rescheduled"
            );
        }

        order.setDeliveryDate(
                request.deliveryDate()
        );

        order.setDeliveryTime(
                request.deliveryTime()
                        .trim()
        );

        Order updatedOrder =
                orderRepository.save(
                        order
                );

        return toB2COrderResponse(
                updatedOrder
        );
    }

    @Transactional
    public B2COrderResponse settleOrder(
            UUID id
    ) {

        Order order =
                getOrder(
                        id
                );

        if (
                order.getStatus()
                        == Order.OrderStatus.CANCELLED
        ) {

            throw new RuntimeException(
                    "Cancelled order cannot be settled"
            );
        }

        if (
                order.isSettled()
        ) {

            throw new RuntimeException(
                    "Order is already settled"
            );
        }

        order.setSettled(
                true
        );

        Order updatedOrder =
                orderRepository.save(
                        order
                );

        return toB2COrderResponse(
                updatedOrder
        );
    }

    @Transactional
    public B2COrderResponse updateStorageLabel(
            UUID id,
            String storageLabel
    ) {

        if (
                storageLabel == null ||
                storageLabel.isBlank()
        ) {

            throw new RuntimeException(
                    "Storage label is required"
            );
        }

        Order order =
                getOrder(
                        id
                );

        if (
                order.getStatus()
                        == Order.OrderStatus.CANCELLED
        ) {

            throw new RuntimeException(
                    "Cancelled order cannot update storage label"
            );
        }

        order.setStorageLabel(
                storageLabel.trim()
        );

        Order updatedOrder =
                orderRepository.save(
                        order
                );

        return toB2COrderResponse(
                updatedOrder
        );
    }

    private void validateStatusChange(
            Order order,
            Order.OrderStatus newStatus
    ) {

        Order.OrderStatus currentStatus =
                order.getStatus();

        if (
                currentStatus == newStatus
        ) {
            return;
        }

        if (
                currentStatus
                        == Order.OrderStatus.CANCELLED
        ) {

            throw new RuntimeException(
                    "Cancelled order status cannot be changed"
            );
        }

        if (
                currentStatus
                        == Order.OrderStatus.DELIVERED
        ) {

            throw new RuntimeException(
                    "Delivered order status cannot be changed"
            );
        }

        if (
                newStatus
                        == Order.OrderStatus.CANCELLED
        ) {
            return;
        }

        if (
                currentStatus
                        == Order.OrderStatus.NEW_ORDER
                &&
                newStatus
                        == Order.OrderStatus.PROCESSING_AT_STORE
        ) {
            return;
        }

        if (
                currentStatus
                        == Order.OrderStatus.PROCESSING_AT_STORE
                &&
                newStatus
                        == Order.OrderStatus.READY_ORDER
        ) {
            return;
        }

        if (
                currentStatus
                        == Order.OrderStatus.READY_ORDER
                &&
                newStatus
                        == Order.OrderStatus.DELIVERED
        ) {
            return;
        }

        throw new RuntimeException(
                "Invalid order status transition"
        );
    }

    private Order getOrder(
            UUID id
    ) {

        return orderRepository
                .findById(
                        id
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found"
                        )
                );
    }

    private B2COrderResponse toB2COrderResponse(
            Order order
    ) {

        return new B2COrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer()
                        .getName(),
                order.getCustomer()
                        .getPhone(),
                order.getTotalAmount(),
                order.getPickupDate(),
                order.getPickupTime(),
                order.getDeliveryDate(),
                order.getDeliveryTime(),
                order.getStorageLabel(),
                order.isHomeDelivery(),
                order.isSettled(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderResponse toOrderResponse(
            Order order,
            String message
    ) {

        OrderResponse.CustomerResponse
                customerResponse =
                new OrderResponse.CustomerResponse(
                        order.getCustomer()
                                .getId(),
                        order.getCustomer()
                                .getName(),
                        order.getCustomer()
                                .getPhone()
                );

        List<OrderResponse.OrderItemResponse>
                items =
                order.getItems()
                        .stream()
                        .map(item ->
                                new OrderResponse.OrderItemResponse(
                                        item.getId(),
                                        item.getProductId(),
                                        item.getProductName(),
                                        item.getProductTypeId(),
                                        item.getProductTypeName(),
                                        item.getServiceId(),
                                        item.getServiceName(),
                                        item.getUnit(),
                                        item.getQuantity(),
                                        item.getUnitPrice(),
                                        item.getLineTotal()
                                )
                        )
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                customerResponse,
                items,
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getCouponCode(),
                order.getExpressChargePercentage(),
                order.getExpressChargeAmount(),
                order.getTotalAmount(),
                order.getPickupDate(),
                order.getPickupTime(),
                order.getDeliveryDate(),
                order.getDeliveryTime(),
                order.getStorageLabel(),
                order.isHomeDelivery(),
                order.isSettled(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                message
        );
    }
}