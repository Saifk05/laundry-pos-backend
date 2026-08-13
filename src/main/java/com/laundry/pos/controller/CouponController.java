package com.laundry.pos.controller;

import com.laundry.pos.request.CouponRequest;
import com.laundry.pos.response.CouponResponse;
import com.laundry.pos.service.CouponService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(
            @RequestBody CouponRequest request
    ) {

        CouponResponse response =
                couponService.createCoupon(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<CouponResponse.CouponListResponse> getAllCoupons() {

        return ResponseEntity.ok(
                couponService.getAllCoupons()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponResponse> getCouponById(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                couponService.getCouponById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable UUID id,
            @RequestBody CouponRequest request
    ) {

        return ResponseEntity.ok(
                couponService.updateCoupon(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(
            @PathVariable UUID id
    ) {

        couponService.deleteCoupon(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}