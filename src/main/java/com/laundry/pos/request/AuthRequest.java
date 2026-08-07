package com.laundry.pos.request;

public record AuthRequest(
        String firstName,
        String lastName,
        String email,
        String password
) {
}