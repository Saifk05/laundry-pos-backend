package com.laundry.pos.response;

import java.util.UUID;

public record ProductServiceResponse(
        UUID serviceId,
        String name,
        Integer price
) {
}