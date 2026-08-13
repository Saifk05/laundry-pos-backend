package com.laundry.pos.request;

import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
        String name,
        String category,
        String icon,
        String pricingUnit,
        List<String> variants,
        List<ServiceRequest> services,
        List<RequirementRequest> requirements
) {

    public record ServiceRequest(
            String name,
            BigDecimal price
    ) {
    }

    public record RequirementRequest(
            String name,
            BigDecimal price
    ) {
    }
}