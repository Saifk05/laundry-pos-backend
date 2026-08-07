package com.laundry.pos.response;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String phone
) {
}