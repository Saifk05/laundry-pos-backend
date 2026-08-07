package com.laundry.pos.response;

import java.util.UUID;

public record AuthResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String token,
        String message
) {
}