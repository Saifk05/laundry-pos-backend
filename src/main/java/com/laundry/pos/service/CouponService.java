package com.laundry.pos.service;

import com.laundry.pos.model.Coupon;
import com.laundry.pos.repository.CouponRepository;
import com.laundry.pos.request.CouponRequest;
import com.laundry.pos.response.CouponResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public CouponResponse createCoupon(
            CouponRequest request
    ) {

        validateCoupon(request);

        String code = request.code()
                .trim()
                .toUpperCase();

        if (couponRepository.existsByCodeIgnoreCase(code)) {
            throw new RuntimeException(
                    "Coupon code already exists"
            );
        }

        Coupon coupon = new Coupon();

        coupon.setCode(code);
        coupon.setDiscountType(
                request.discountType()
        );
        coupon.setDiscountValue(
                request.discountValue()
        );
        coupon.setMinimumOrderAmount(
                request.minimumOrderAmount()
        );
        coupon.setActive(
                request.active()
        );

        Coupon savedCoupon =
                couponRepository.save(coupon);

        return toResponse(savedCoupon);
    }

    public CouponResponse.CouponListResponse getAllCoupons() {

        List<CouponResponse> coupons =
                couponRepository.findAll()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        if (coupons.isEmpty()) {

            return new CouponResponse.CouponListResponse(
                    "No coupons available",
                    coupons
            );
        }

        return new CouponResponse.CouponListResponse(
                "Coupons fetched successfully",
                coupons
        );
    }

    public CouponResponse getCouponById(
            UUID id
    ) {

        Coupon coupon =
                couponRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Coupon not found"
                                )
                        );

        return toResponse(coupon);
    }

    public CouponResponse updateCoupon(
            UUID id,
            CouponRequest request
    ) {

        validateCoupon(request);

        Coupon coupon =
                couponRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Coupon not found"
                                )
                        );

        String code = request.code()
                .trim()
                .toUpperCase();

        couponRepository
                .findByCodeIgnoreCase(code)
                .ifPresent(existingCoupon -> {

                    if (!existingCoupon
                            .getId()
                            .equals(id)) {

                        throw new RuntimeException(
                                "Coupon code already exists"
                        );
                    }
                });

        coupon.setCode(code);

        coupon.setDiscountType(
                request.discountType()
        );

        coupon.setDiscountValue(
                request.discountValue()
        );

        coupon.setMinimumOrderAmount(
                request.minimumOrderAmount()
        );

        coupon.setActive(
                request.active()
        );

        Coupon updatedCoupon =
                couponRepository.save(coupon);

        return toResponse(updatedCoupon);
    }

    public void deleteCoupon(
            UUID id
    ) {

        Coupon coupon =
                couponRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Coupon not found"
                                )
                        );

        coupon.setActive(false);

        couponRepository.save(coupon);
    }

    private void validateCoupon(
            CouponRequest request
    ) {

        if (
                request.code() == null ||
                request.code().isBlank()
        ) {

            throw new RuntimeException(
                    "Coupon code is required"
            );
        }

        if (request.discountType() == null) {

            throw new RuntimeException(
                    "Discount type is required"
            );
        }

        if (
                request.discountValue() == null ||
                request.discountValue()
                        .signum() <= 0
        ) {

            throw new RuntimeException(
                    "Discount value must be greater than 0"
            );
        }

        if (
                request.minimumOrderAmount() == null ||
                request.minimumOrderAmount()
                        .signum() < 0
        ) {

            throw new RuntimeException(
                    "Minimum order amount cannot be negative"
            );
        }

        if (
                request.discountType()
                        == Coupon.DiscountType.PERCENTAGE
                        &&
                        request.discountValue()
                                .compareTo(
                                        BigDecimal.valueOf(100)
                                ) > 0
        ) {

            throw new RuntimeException(
                    "Percentage discount cannot exceed 100"
            );
        }
    }

    private CouponResponse toResponse(
            Coupon coupon
    ) {

        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getMinimumOrderAmount(),
                coupon.isActive()
        );
    }
}