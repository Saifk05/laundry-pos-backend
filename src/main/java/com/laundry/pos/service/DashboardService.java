package com.laundry.pos.service;

import com.laundry.pos.model.Order;

import com.laundry.pos.repository.OrderRepository;

import com.laundry.pos.response.DashboardResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final OrderRepository orderRepository;

    public DashboardService(
            OrderRepository orderRepository
    ) {

        this.orderRepository =
                orderRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {

        List<Order> allOrders =
                orderRepository
                        .findAllByOrderByCreatedAtDesc();

        List<Order> dashboardOrders =
                allOrders
                        .stream()
                        .filter(order ->
                                order.getDeliveryDate() != null
                        )
                        .filter(order ->
                                order.getStatus()
                                        != Order.OrderStatus.CANCELLED
                        )
                        .filter(order ->
                                order.getStatus()
                                        != Order.OrderStatus.DELIVERED
                        )
                        .toList();

        int totalOrders =
                dashboardOrders.size();

        int processingOrders =
                (int) dashboardOrders
                        .stream()
                        .filter(order ->
                                order.getStatus()
                                        == Order.OrderStatus.PROCESSING_AT_STORE
                        )
                        .count();

        int readyOrders =
                (int) dashboardOrders
                        .stream()
                        .filter(order ->
                                order.getStatus()
                                        == Order.OrderStatus.READY_ORDER
                        )
                        .count();

        Map<LocalDate, List<Order>>
                groupedOrders =
                new LinkedHashMap<>();

        dashboardOrders
                .stream()
                .sorted(
                        Comparator.comparing(
                                Order::getDeliveryDate,
                                Comparator.reverseOrder()
                        )
                )
                .forEach(order -> {

                    groupedOrders
                            .computeIfAbsent(
                                    order.getDeliveryDate(),
                                    key ->
                                            new ArrayList<>()
                            )
                            .add(
                                    order
                            );
                });

        List<DashboardResponse.DeliveryDateResponse>
                dates =
                groupedOrders
                        .entrySet()
                        .stream()
                        .map(entry ->
                                toDeliveryDateResponse(
                                        entry.getKey(),
                                        entry.getValue()
                                )
                        )
                        .toList();

        return new DashboardResponse(
                totalOrders,
                processingOrders,
                readyOrders,
                dates
        );
    }

    private DashboardResponse.DeliveryDateResponse
    toDeliveryDateResponse(
            LocalDate deliveryDate,
            List<Order> orders
    ) {

        int totalOrders =
                orders.size();

        BigDecimal totalPieces =
                orders
                        .stream()
                        .map(this::calculatePieces)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        List<Order> processingOrderList =
                orders
                        .stream()
                        .filter(order ->
                                order.getStatus()
                                        == Order.OrderStatus.PROCESSING_AT_STORE
                        )
                        .toList();

        List<Order> readyOrderList =
                orders
                        .stream()
                        .filter(order ->
                                order.getStatus()
                                        == Order.OrderStatus.READY_ORDER
                        )
                        .toList();

        BigDecimal processingPieces =
                processingOrderList
                        .stream()
                        .map(this::calculatePieces)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal readyPieces =
                readyOrderList
                        .stream()
                        .map(this::calculatePieces)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        List<DashboardResponse.DashboardOrderResponse>
                orderResponses =
                orders
                        .stream()
                        .map(this::toOrderResponse)
                        .toList();

        String dayLabel =
                deliveryDate.format(
                        DateTimeFormatter.ofPattern(
                                "EEE d MMM"
                        )
                );

        return new DashboardResponse.DeliveryDateResponse(
                deliveryDate,
                dayLabel,

                totalOrders,
                totalPieces,

                processingOrderList.size(),
                processingPieces,

                readyOrderList.size(),
                readyPieces,

                orderResponses
        );
    }

    private DashboardResponse.DashboardOrderResponse
    toOrderResponse(
            Order order
    ) {

        return new DashboardResponse.DashboardOrderResponse(
                order.getId(),
                order.getOrderNumber(),

                order.getCustomer()
                        .getName(),

                order.getCustomer()
                        .getPhone(),

                order.getTotalAmount(),

                calculatePieces(
                        order
                ),

                order.getDeliveryTime(),

                order.getStatus()
        );
    }

    private BigDecimal calculatePieces(
            Order order
    ) {

        return order.getItems()
                .stream()
                .map(item ->
                        item.getQuantity() != null
                                ? item.getQuantity()
                                : BigDecimal.ZERO
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }
}