package com.laundry.pos.response;

import java.math.BigDecimal;
import java.util.List;

public record BillListResponse(
        String message,

        int totalBills,

        BigDecimal totalPaidAmount,

        BigDecimal totalDueAmount,

        BigDecimal totalAmount,

        BigDecimal totalTax,

        BigDecimal totalTaxableAmount,

        BigDecimal totalExpressAmount,

        BigDecimal totalDiscountAmount,

        BigDecimal totalGrossAmount,

        List<BillResponse> bills
) {
}