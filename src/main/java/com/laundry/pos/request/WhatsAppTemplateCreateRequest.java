package com.laundry.pos.request;

public record WhatsAppTemplateCreateRequest(
        String name,
        String category,
        String language,
        String body
) {
}