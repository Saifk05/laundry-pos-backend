package com.laundry.pos.request;

public record CustomerRequest(
        String name,
        String phone
) {
}