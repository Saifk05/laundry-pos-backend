package com.laundry.pos.response;

public record BusinessSettingResponse(
        Long id,
        String businessName,
        String headerSubtitle,
        String adminName,
        String adminSubtitle,
        String logoUrl
) {
}