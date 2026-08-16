package com.laundry.pos.service;

import com.laundry.pos.model.Order;
import com.laundry.pos.model.Product;
import com.laundry.pos.repository.OrderRepository;
import com.laundry.pos.response.SalesReportResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
public class SalesReportService {

    private final OrderRepository orderRepository;


    public SalesReportService(
            OrderRepository orderRepository
    ) {
        this.orderRepository =
                orderRepository;
    }


    @Transactional(readOnly = true)
    public SalesReportResponse getSalesReport(
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (startDate == null) {
            throw new IllegalArgumentException(
                    "Start date is required"
            );
        }

        if (endDate == null) {
            throw new IllegalArgumentException(
                    "End date is required"
            );
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date"
            );
        }


        LocalDateTime startDateTime =
                startDate.atStartOfDay();

        LocalDateTime endDateTime =
                endDate
                        .plusDays(1)
                        .atStartOfDay();


        List<Order> orders =
                orderRepository
                        .findSalesReportOrders(
                                startDateTime,
                                endDateTime
                        );


        BigDecimal totalSales =
                BigDecimal.ZERO;

        BigDecimal expressAmount =
                BigDecimal.ZERO;

        long totalExpressOrders =
                0;


        Map<String, ProductAccumulator>
                productMap =
                new LinkedHashMap<>();

        Map<String, ServiceAccumulator>
                serviceMap =
                new LinkedHashMap<>();

        List<SalesReportResponse.SalesOrder>
                salesOrders =
                new ArrayList<>();


        for (Order order : orders) {

            BigDecimal orderTotal =
                    safeAmount(
                            order.getTotalAmount()
                    );

            BigDecimal orderExpress =
                    safeAmount(
                            order.getExpressChargeAmount()
                    );


            totalSales =
                    totalSales.add(
                            orderTotal
                    );

            expressAmount =
                    expressAmount.add(
                            orderExpress
                    );


            if (
                    orderExpress.compareTo(
                            BigDecimal.ZERO
                    ) > 0
            ) {

                totalExpressOrders++;
            }


            for (
                    Order.OrderItem item :
                    order.getItems()
            ) {

                String productKey =
                        item.getProductId()
                                .toString();


                ProductAccumulator product =
                        productMap.computeIfAbsent(
                                productKey,
                                key ->
                                        new ProductAccumulator(
                                                item.getProductName(),
                                                item.getUnit()
                                        )
                        );


                product.quantity =
                        product.quantity.add(
                                safeAmount(
                                        item.getQuantity()
                                )
                        );


                product.revenue =
                        product.revenue.add(
                                safeAmount(
                                        item.getLineTotal()
                                )
                        );


                String serviceKey =
                        item.getServiceId()
                                .toString();


                ServiceAccumulator service =
                        serviceMap.computeIfAbsent(
                                serviceKey,
                                key ->
                                        new ServiceAccumulator(
                                                item.getServiceName()
                                        )
                        );


                service.revenue =
                        service.revenue.add(
                                safeAmount(
                                        item.getLineTotal()
                                )
                        );


                if (
                        !service.orderNumbers
                                .contains(
                                        order.getOrderNumber()
                                )
                ) {

                    service.orderNumbers.add(
                            order.getOrderNumber()
                    );
                }
            }


            salesOrders.add(
                    new SalesReportResponse.SalesOrder(
                            order.getOrderNumber(),
                            order.getCustomer()
                                    .getName(),
                            order.getCreatedAt()
                                    .toLocalDate(),
                            safeAmount(
                                    order.getDiscountAmount()
                            ),
                            orderExpress,
                            orderTotal,
                            order.getStatus()
                    )
            );
        }


        long totalOrders =
                orders.size();


        BigDecimal averageOrderValue =
                totalOrders > 0
                        ? totalSales.divide(
                                BigDecimal.valueOf(
                                        totalOrders
                                ),
                                2,
                                RoundingMode.HALF_UP
                        )
                        : BigDecimal.ZERO;


        List<SalesReportResponse.ProductSales>
                productSales =
                productMap
                        .values()
                        .stream()
                        .map(
                                product ->
                                        new SalesReportResponse.ProductSales(
                                                product.productName,
                                                product.quantity,
                                                product.unit,
                                                product.revenue
                                        )
                        )
                        .sorted(
                                (
                                        first,
                                        second
                                ) ->
                                        second
                                                .revenue()
                                                .compareTo(
                                                        first.revenue()
                                                )
                        )
                        .toList();


        List<SalesReportResponse.ServiceSales>
                serviceSales =
                serviceMap
                        .values()
                        .stream()
                        .map(
                                service ->
                                        new SalesReportResponse.ServiceSales(
                                                service.serviceName,
                                                service.orderNumbers.size(),
                                                service.revenue
                                        )
                        )
                        .sorted(
                                (
                                        first,
                                        second
                                ) ->
                                        second
                                                .revenue()
                                                .compareTo(
                                                        first.revenue()
                                                )
                        )
                        .toList();


        SalesReportResponse.Summary summary =
                new SalesReportResponse.Summary(
                        totalSales,
                        totalOrders,
                        averageOrderValue,
                        expressAmount,
                        totalExpressOrders
                );


        return new SalesReportResponse(
                startDate,
                endDate,
                summary,
                productSales,
                serviceSales,
                salesOrders
        );
    }


    private BigDecimal safeAmount(
            BigDecimal amount
    ) {

        return amount != null
                ? amount
                : BigDecimal.ZERO;
    }


    private static class ProductAccumulator {

        private final String productName;

        private final Product.PricingUnit unit;

        private BigDecimal quantity =
                BigDecimal.ZERO;

        private BigDecimal revenue =
                BigDecimal.ZERO;


        private ProductAccumulator(
                String productName,
                Product.PricingUnit unit
        ) {

            this.productName =
                    productName;

            this.unit =
                    unit;
        }
    }


    private static class ServiceAccumulator {

        private final String serviceName;

        private final List<String>
                orderNumbers =
                new ArrayList<>();

        private BigDecimal revenue =
                BigDecimal.ZERO;


        private ServiceAccumulator(
                String serviceName
        ) {

            this.serviceName =
                    serviceName;
        }
    }
}