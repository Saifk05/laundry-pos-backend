package com.laundry.pos.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RetagOrderRequest(
        List<Item> items
) {

    public record Item(
            UUID orderItemId,
            BigDecimal quantity
    ) {
    }
}