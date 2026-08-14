package com.laundry.pos.controller;

import com.laundry.pos.model.Order;
import com.laundry.pos.request.OrderStatusRequest;
import com.laundry.pos.request.RescheduleOrderRequest;
import com.laundry.pos.response.B2COrderListResponse;
import com.laundry.pos.response.B2COrderResponse;
import com.laundry.pos.response.OrderResponse;
import com.laundry.pos.service.OrderService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(
            OrderService orderService
    ) {
        this.orderService =
                orderService;
    }

    @GetMapping
    public ResponseEntity<B2COrderListResponse> getOrders(
            @RequestParam(
                    required = false
            )
            Order.OrderStatus status,

            @RequestParam(
                    required = false
            )
            String search
    ) {

        return ResponseEntity.ok(
                orderService.getOrders(
                        status,
                        search
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                orderService.getOrderById(
                        id
                )
        );
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByNumber(
            @PathVariable String orderNumber
    ) {

        return ResponseEntity.ok(
                orderService.getOrderByNumber(
                        orderNumber
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<B2COrderResponse> updateStatus(
            @PathVariable UUID id,
            @RequestBody OrderStatusRequest request
    ) {

        return ResponseEntity.ok(
                orderService.updateStatus(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/ready")
    public ResponseEntity<B2COrderResponse> markReady(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                orderService.markReady(
                        id
                )
        );
    }

    @PatchMapping("/{id}/delivered")
    public ResponseEntity<B2COrderResponse> markDelivered(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                orderService.markDelivered(
                        id
                )
        );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<B2COrderResponse> cancelOrder(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                orderService.cancelOrder(
                        id
                )
        );
    }

    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<B2COrderResponse> rescheduleOrder(
            @PathVariable UUID id,
            @RequestBody RescheduleOrderRequest request
    ) {

        return ResponseEntity.ok(
                orderService.rescheduleOrder(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/settle")
    public ResponseEntity<B2COrderResponse> settleOrder(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                orderService.settleOrder(
                        id
                )
        );
    }

    @PatchMapping("/{id}/storage-label")
    public ResponseEntity<B2COrderResponse> updateStorageLabel(
            @PathVariable UUID id,
            @RequestParam String storageLabel
    ) {

        return ResponseEntity.ok(
                orderService.updateStorageLabel(
                        id,
                        storageLabel
                )
        );
    }
}