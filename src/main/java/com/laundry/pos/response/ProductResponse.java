package com.laundry.pos.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String category,
        String icon,
        String pricingUnit,
        List<VariantResponse> variants,
        List<ServiceResponse> services,
        List<RequirementResponse> requirements
) {

    public record VariantResponse(
            UUID id,
            String name
    ) {
    }

    public record ServiceResponse(
            UUID serviceId,
            String name,
            BigDecimal price
    ) {
    }

    public record RequirementResponse(
            UUID requirementId,
            String name,
            BigDecimal price
    ) {
    }
}