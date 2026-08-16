package com.laundry.pos.response;

import com.laundry.pos.model.Order;
import com.laundry.pos.model.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SalesReportResponse(
        LocalDate startDate,
        LocalDate endDate,
        Summary summary,
        List<ProductSales> productSales,
        List<ServiceSales> serviceSales,
        List<SalesOrder> orders
) {

    public record Summary(
            BigDecimal totalSales,
            long totalOrders,
            BigDecimal averageOrderValue,
            BigDecimal expressAmount,
            long totalExpressOrders
    ) {
    }


    public record ProductSales(
            String productName,
            BigDecimal quantity,
            Product.PricingUnit unit,
            BigDecimal revenue
    ) {
    }


    public record ServiceSales(
            String serviceName,
            long orders,
            BigDecimal revenue
    ) {
    }


    public record SalesOrder(
            String orderNumber,
            String customerName,
            LocalDate date,
            BigDecimal discountAmount,
            BigDecimal expressAmount,
            BigDecimal totalAmount,
            Order.OrderStatus status
    ) {
    }
}