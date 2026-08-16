package com.laundry.pos.response;

import java.util.List;

public record BulkProductResponse(
        String message,
        int totalProducts,
        int createdProducts,
        int updatedProducts,
        List<ProductResponse> products
) {
}