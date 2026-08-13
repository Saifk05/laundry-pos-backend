package com.laundry.pos.controller;

import com.laundry.pos.request.ExpressChargeRequest;
import com.laundry.pos.response.ExpressChargeResponse;
import com.laundry.pos.service.ExpressChargeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/express-charges")
public class ExpressChargeController {

    private final ExpressChargeService expressChargeService;

    public ExpressChargeController(
            ExpressChargeService expressChargeService
    ) {
        this.expressChargeService = expressChargeService;
    }

    @PostMapping
    public ResponseEntity<ExpressChargeResponse> createExpressCharge(
            @RequestBody ExpressChargeRequest request
    ) {

        ExpressChargeResponse response =
                expressChargeService.createExpressCharge(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<
            ExpressChargeResponse.ExpressChargeListResponse
            > getAllExpressCharges() {

        return ResponseEntity.ok(
                expressChargeService.getAllExpressCharges()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpressChargeResponse> getExpressChargeById(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                expressChargeService.getExpressChargeById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpressChargeResponse> updateExpressCharge(
            @PathVariable UUID id,
            @RequestBody ExpressChargeRequest request
    ) {

        return ResponseEntity.ok(
                expressChargeService.updateExpressCharge(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ExpressChargeResponse> updateExpressChargeStatus(
            @PathVariable UUID id,
            @RequestParam boolean active
    ) {

        return ResponseEntity.ok(
                expressChargeService.updateStatus(
                        id,
                        active
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpressCharge(
            @PathVariable UUID id
    ) {

        expressChargeService.deleteExpressCharge(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}