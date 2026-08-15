package com.laundry.pos.service;

import com.laundry.pos.model.Order;
import com.laundry.pos.repository.OrderRepository;
import com.laundry.pos.request.OrderStatusRequest;
import com.laundry.pos.request.RescheduleOrderRequest;
import com.laundry.pos.request.RetagOrderRequest;
import com.laundry.pos.response.B2COrderListResponse;
import com.laundry.pos.response.B2COrderResponse;
import com.laundry.pos.response.OrderResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final WhatsAppService whatsAppService;

    public OrderService(
            OrderRepository orderRepository,
            WhatsAppService whatsAppService
    ) {
        this.orderRepository =
                orderRepository;

        this.whatsAppService =
                whatsAppService;
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

        Order.OrderStatus previousStatus =
                order.getStatus();

        order.setStatus(
                request.status()
        );

        Order updatedOrder =
                orderRepository.save(
                        order
                );

        if (
                previousStatus
                        == Order.OrderStatus.TAGGED
                        &&
                updatedOrder.getStatus()
                        == Order.OrderStatus.PROCESSING_AT_STORE
        ) {

            try {

                whatsAppService.sendProcessingMessage(
                        updatedOrder
                                .getCustomer()
                                .getPhone(),

                        updatedOrder
                                .getCustomer()
                                .getName(),

                        updatedOrder
                                .getOrderNumber()
                );

            } catch (Exception exception) {

                System.out.println(
                        "WhatsApp processing message failed: "
                                + exception.getMessage()
                );
            }
        }

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

        try {

            whatsAppService.sendReadyMessage(
                    updatedOrder
                            .getCustomer()
                            .getPhone(),

                    updatedOrder
                            .getCustomer()
                            .getName(),

                    updatedOrder
                            .getOrderNumber()
            );

        } catch (Exception exception) {

            System.out.println(
                    "WhatsApp ready message failed: "
                            + exception.getMessage()
            );
        }

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

        order.setDeliveredAt(
                LocalDateTime.now()
        );

        Order updatedOrder =
                orderRepository.save(
                        order
                );

        try {

            whatsAppService.sendDeliveredMessage(
                    updatedOrder
                            .getCustomer()
                            .getPhone(),

                    updatedOrder
                            .getCustomer()
                            .getName(),

                    updatedOrder
                            .getOrderNumber()
            );

        } catch (Exception exception) {

            System.out.println(
                    "WhatsApp delivered message failed: "
                            + exception.getMessage()
            );
        }

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

        try {

            whatsAppService.sendCancelledMessage(
                    updatedOrder
                            .getCustomer()
                            .getPhone(),

                    updatedOrder
                            .getCustomer()
                            .getName(),

                    updatedOrder
                            .getOrderNumber()
            );

        } catch (Exception exception) {

            System.out.println(
                    "WhatsApp cancelled message failed: "
                            + exception.getMessage()
            );
        }

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

    @Transactional
    public OrderResponse retagOrder(
            UUID id,
            RetagOrderRequest request
    ) {

        if (
                request == null ||
                request.items() == null ||
                request.items().isEmpty()
        ) {

            throw new RuntimeException(
                    "Re-tag items are required"
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
                    "Delivered order cannot be re-tagged"
            );
        }

        if (
                order.getStatus()
                        == Order.OrderStatus.CANCELLED
        ) {

            throw new RuntimeException(
                    "Cancelled order cannot be re-tagged"
            );
        }

        if (
                order.isSettled()
        ) {

            throw new RuntimeException(
                    "Settled order cannot be re-tagged"
            );
        }

        for (
                RetagOrderRequest.Item requestItem
                        : request.items()
        ) {

            if (
                    requestItem == null ||
                    requestItem.orderItemId() == null
            ) {

                throw new RuntimeException(
                        "Order item id is required"
                );
            }

            Order.OrderItem orderItem =
                    order.getItems()
                            .stream()
                            .filter(item ->
                                    item.getId()
                                            .equals(
                                                    requestItem
                                                            .orderItemId()
                                            )
                            )
                            .findFirst()
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Order item not found"
                                    )
                            );

            BigDecimal quantity =
                    requestItem.quantity();

            if (
                    quantity == null ||
                    quantity.compareTo(
                            BigDecimal.ZERO
                    ) < 0
            ) {

                throw new RuntimeException(
                        "Invalid quantity"
                );
            }

            if (
                    quantity.compareTo(
                            orderItem.getQuantity()
                    ) > 0
            ) {

                throw new RuntimeException(
                        "Re-tag quantity cannot exceed original quantity"
                );
            }

            orderItem.setQuantity(
                    quantity
            );

            orderItem.setLineTotal(
                    orderItem
                            .getUnitPrice()
                            .multiply(
                                    quantity
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
            );
        }

        order.getItems()
                .removeIf(
                        item ->
                                item.getQuantity()
                                        .compareTo(
                                                BigDecimal.ZERO
                                        ) == 0
                );

        if (
                order.getItems()
                        .isEmpty()
        ) {

            throw new RuntimeException(
                    "Order must contain at least one item"
            );
        }

        BigDecimal subtotal =
                order.getItems()
                        .stream()
                        .map(
                                Order.OrderItem
                                        ::getLineTotal
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        order.setSubtotal(
                subtotal
        );

        BigDecimal discountAmount =
                order.getDiscountAmount() != null
                        ? order.getDiscountAmount()
                        : BigDecimal.ZERO;

        if (
                discountAmount.compareTo(
                        subtotal
                ) > 0
        ) {

            discountAmount =
                    subtotal;
        }

        discountAmount =
                discountAmount.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        order.setDiscountAmount(
                discountAmount
        );

        BigDecimal afterDiscount =
                subtotal.subtract(
                        discountAmount
                );

        BigDecimal expressChargeAmount =
                BigDecimal.ZERO;

        if (
                order.getExpressChargePercentage()
                        != null &&
                order.getExpressChargePercentage()
                        .compareTo(
                                BigDecimal.ZERO
                        ) > 0
        ) {

            expressChargeAmount =
                    afterDiscount
                            .multiply(
                                    order.getExpressChargePercentage()
                            )
                            .divide(
                                    BigDecimal.valueOf(
                                            100
                                    ),
                                    2,
                                    RoundingMode.HALF_UP
                            );
        }

        order.setExpressChargeAmount(
                expressChargeAmount
        );

        BigDecimal totalAmount =
                afterDiscount
                        .add(
                                expressChargeAmount
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        order.setTotalAmount(
                totalAmount
        );

        BigDecimal paidAmount =
                order.getPaidAmount() != null
                        ? order.getPaidAmount()
                        : BigDecimal.ZERO;

        BigDecimal balanceAmount =
                totalAmount.subtract(
                        paidAmount
                );

        if (
                balanceAmount.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {

            balanceAmount =
                    BigDecimal.ZERO;
        }

        order.setBalanceAmount(
                balanceAmount.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
        );

        if (
                paidAmount.compareTo(
                        BigDecimal.ZERO
                ) == 0
        ) {

            order.setPaymentStatus(
                    Order.PaymentStatus.PENDING
            );

            order.setSettled(
                    false
            );

        } else if (
                paidAmount.compareTo(
                        totalAmount
                ) >= 0
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

        return toOrderResponse(
                updatedOrder,
                "Order re-tagged successfully"
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
                        == Order.OrderStatus.TAGGED
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

        OrderResponse.CustomerResponse customerResponse =
                new OrderResponse.CustomerResponse(
                        order.getCustomer()
                                .getId(),

                        order.getCustomer()
                                .getName(),

                        order.getCustomer()
                                .getPhone()
                );

        List<OrderResponse.OrderItemResponse> items =
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

                order.getPaidAmount(),

                order.getBalanceAmount(),

                order.getPaymentStatus(),

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