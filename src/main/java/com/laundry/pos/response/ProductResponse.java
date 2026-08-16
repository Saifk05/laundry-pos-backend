package com.laundry.pos.response;

import com.laundry.pos.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String icon,
        Product.PricingUnit unit,
        boolean active,
        List<TypeResponse> types
) {

    public record TypeResponse(
            UUID id,
            String name,
            List<ServiceResponse> services
    ) {
    }

    public record ServiceResponse(
            UUID id,
            String name,
            BigDecimal price
    ) {
    }

    public record ProductListResponse(
            String message,
            List<ProductResponse> products
    ) {
    }
}