package com.laundry.pos.request;

import com.laundry.pos.model.Product;

import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
        String name,
        String icon,
        Product.PricingUnit unit,
        boolean active,
        List<TypeRequest> types
) {

    public record TypeRequest(
            String name,
            List<ServiceRequest> services
    ) {
    }

    public record ServiceRequest(
            String name,
            BigDecimal price
    ) {
    }
}