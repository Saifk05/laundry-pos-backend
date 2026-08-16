package com.laundry.pos.request;

public record BusinessSettingRequest(
        String businessName,
        String headerSubtitle,
        String adminName,
        String adminSubtitle,
        String logoUrl
) {
}