package com.laundry.pos.controller;

import com.laundry.pos.request.PaymentRequest;
import com.laundry.pos.response.PaymentHistoryResponse;
import com.laundry.pos.response.SettlementResponse;
import com.laundry.pos.service.SettlementService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/settlements")
@CrossOrigin
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(
            SettlementService settlementService
    ) {
        this.settlementService =
                settlementService;
    }

    @GetMapping
    public ResponseEntity<List<SettlementResponse>>
    getSettlements() {

        return ResponseEntity.ok(
                settlementService
                        .getSettlements()
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<SettlementResponse>
    getSettlement(
            @PathVariable
            UUID orderId
    ) {

        return ResponseEntity.ok(
                settlementService
                        .getSettlement(
                                orderId
                        )
        );
    }

    @PostMapping("/{orderId}/payments")
    public ResponseEntity<SettlementResponse>
    addPayment(
            @PathVariable
            UUID orderId,

            @RequestBody
            PaymentRequest request
    ) {

        return ResponseEntity.ok(
                settlementService
                        .addPayment(
                                orderId,
                                request
                        )
        );
    }

    @GetMapping("/{orderId}/payments")
    public ResponseEntity<PaymentHistoryResponse>
    getPaymentHistory(
            @PathVariable
            UUID orderId
    ) {

        return ResponseEntity.ok(
                settlementService
                        .getPaymentHistory(
                                orderId
                        )
        );
    }
}