package com.laundry.pos.controller;

import com.laundry.pos.request.WalkInOrderRequest;
import com.laundry.pos.response.OrderResponse;
import com.laundry.pos.response.WalkInSetupResponse;
import com.laundry.pos.service.WalkInService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/walk-in")
public class WalkInController {

    private final WalkInService walkInService;

    public WalkInController(
            WalkInService walkInService
    ) {
        this.walkInService = walkInService;
    }


    @GetMapping("/setup")
    public ResponseEntity<WalkInSetupResponse> getSetup() {

        return ResponseEntity.ok(
                walkInService.getSetup()
        );
    }


    @PostMapping
    public ResponseEntity<OrderResponse> createWalkInOrder(
            @RequestBody WalkInOrderRequest request
    ) {

        OrderResponse response =
                walkInService.createWalkInOrder(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}