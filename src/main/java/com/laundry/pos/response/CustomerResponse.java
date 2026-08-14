package com.laundry.pos.response;

import java.util.UUID;

public record CustomerResponse(
        boolean exists,
        String message,
        UUID id,
        String name,
        String phone
) {
}