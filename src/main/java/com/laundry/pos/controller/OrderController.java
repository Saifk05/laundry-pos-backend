package com.laundry.pos.controller;

import com.laundry.pos.request.WalkInOrderRequest;
import com.laundry.pos.response.OrderResponse;
import com.laundry.pos.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(
            OrderService orderService
    ) {
        this.orderService = orderService;
    }

    @PostMapping("/walk-in")
    public ResponseEntity<OrderResponse> createWalkInOrder(
            @RequestBody WalkInOrderRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        orderService.createWalkInOrder(
                                request
                        )
                );
    }
}