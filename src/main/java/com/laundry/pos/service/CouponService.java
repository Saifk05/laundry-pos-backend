package com.laundry.pos.service;

import com.laundry.pos.repository.CouponRepository;
import com.laundry.pos.response.CouponResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(
            CouponRepository couponRepository
    ) {
        this.couponRepository = couponRepository;
    }

    public List<CouponResponse> getAllCoupons() {

        return couponRepository
                .findAll()
                .stream()
                .map(coupon ->
                        new CouponResponse(
                                coupon.getId(),
                                coupon.getCode(),
                                coupon.getAmount(),
                                coupon.isActive()
                        )
                )
                .toList();
    }
}