package com.laundry.pos.response;

import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String category,
        String icon,
        List<ProductServiceResponse> services
) {
}