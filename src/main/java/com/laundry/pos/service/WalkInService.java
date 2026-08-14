package com.laundry.pos.service;

import com.laundry.pos.model.Coupon;
import com.laundry.pos.model.Customer;
import com.laundry.pos.model.ExpressCharge;
import com.laundry.pos.model.Order;
import com.laundry.pos.model.Product;

import com.laundry.pos.repository.CouponRepository;
import com.laundry.pos.repository.CustomerRepository;
import com.laundry.pos.repository.ExpressChargeRepository;
import com.laundry.pos.repository.OrderRepository;
import com.laundry.pos.repository.ProductRepository;

import com.laundry.pos.request.WalkInOrderRequest;

import com.laundry.pos.response.CouponResponse;
import com.laundry.pos.response.ExpressChargeResponse;
import com.laundry.pos.response.OrderResponse;
import com.laundry.pos.response.ProductResponse;
import com.laundry.pos.response.WalkInSetupResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class WalkInService {

    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final ExpressChargeRepository expressChargeRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public WalkInService(
            ProductRepository productRepository,
            CouponRepository couponRepository,
            ExpressChargeRepository expressChargeRepository,
            CustomerRepository customerRepository,
            OrderRepository orderRepository
    ) {

        this.productRepository =
                productRepository;

        this.couponRepository =
                couponRepository;

        this.expressChargeRepository =
                expressChargeRepository;

        this.customerRepository =
                customerRepository;

        this.orderRepository =
                orderRepository;
    }

    public WalkInSetupResponse getSetup() {

        List<ProductResponse> products =
                productRepository
                        .findAllByActiveTrue()
                        .stream()
                        .map(this::toProductResponse)
                        .toList();

        List<CouponResponse> coupons =
                couponRepository
                        .findAllByActiveTrue()
                        .stream()
                        .map(this::toCouponResponse)
                        .toList();

        List<ExpressChargeResponse> expressCharges =
                expressChargeRepository
                        .findAllByActiveTrue()
                        .stream()
                        .map(this::toExpressChargeResponse)
                        .toList();

        return new WalkInSetupResponse(
                "Walk-in setup fetched successfully",
                products,
                coupons,
                expressCharges
        );
    }

    @Transactional
    public OrderResponse createWalkInOrder(
            WalkInOrderRequest request
    ) {

        validateOrderRequest(
                request
        );

        Customer customer =
                findOrCreateCustomer(
                        request.customer()
                );

        List<Order.OrderItem> orderItems =
                new ArrayList<>();

        BigDecimal subtotal =
                BigDecimal.ZERO;

        for (
                WalkInOrderRequest.OrderItemRequest itemRequest
                : request.items()
        ) {

            Order.OrderItem orderItem =
                    buildOrderItem(
                            itemRequest
                    );

            orderItems.add(
                    orderItem
            );

            subtotal =
                    subtotal.add(
                            orderItem.getLineTotal()
                    );
        }

        subtotal =
                money(
                        subtotal
                );

        BigDecimal discountAmount =
                BigDecimal.ZERO;

        String couponCode =
                null;

        if (
                request.couponId() != null
        ) {

            Coupon coupon =
                    couponRepository
                            .findById(
                                    request.couponId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Coupon not found"
                                    )
                            );

            if (
                    !coupon.isActive()
            ) {

                throw new RuntimeException(
                        "Coupon is not active"
                );
            }

            if (
                    subtotal.compareTo(
                            coupon.getMinimumOrderAmount()
                    ) < 0
            ) {

                throw new RuntimeException(
                        "Minimum order amount for coupon "
                                + coupon.getCode()
                                + " is ₹"
                                + coupon.getMinimumOrderAmount()
                );
            }

            discountAmount =
                    calculateDiscount(
                            coupon,
                            subtotal
                    );

            couponCode =
                    coupon.getCode();
        }

        discountAmount =
                money(
                        discountAmount
                );

        BigDecimal amountAfterDiscount =
                subtotal.subtract(
                        discountAmount
                );

        if (
                amountAfterDiscount.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {

            amountAfterDiscount =
                    BigDecimal.ZERO;
        }

        amountAfterDiscount =
                money(
                        amountAfterDiscount
                );

        BigDecimal expressChargePercentage =
                null;

        BigDecimal expressChargeAmount =
                BigDecimal.ZERO;

        if (
                request.expressChargeId() != null
        ) {

            ExpressCharge expressCharge =
                    expressChargeRepository
                            .findById(
                                    request.expressChargeId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Express charge not found"
                                    )
                            );

            if (
                    !expressCharge.isActive()
            ) {

                throw new RuntimeException(
                        "Express charge is not active"
                );
            }

            expressChargePercentage =
                    expressCharge.getPercentage();

            expressChargeAmount =
                    amountAfterDiscount
                            .multiply(
                                    expressChargePercentage
                            )
                            .divide(
                                    BigDecimal.valueOf(100),
                                    2,
                                    RoundingMode.HALF_UP
                            );
        }

        expressChargeAmount =
                money(
                        expressChargeAmount
                );

        BigDecimal totalAmount =
                amountAfterDiscount.add(
                        expressChargeAmount
                );

        totalAmount =
                money(
                        totalAmount
                );

        Long sequence =
                orderRepository
                        .getNextOrderSequence();

        String orderNumber =
                String.format(
                        "LAUNDRY-%04d",
                        sequence
                );

        LocalDate pickupDate =
                LocalDate.now();

        String pickupTime =
                LocalTime.now()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "hh:mm a"
                                )
                        );

        Order order =
                new Order();

        order.setOrderNumber(
                orderNumber
        );

        order.setCustomer(
                customer
        );

        order.setSubtotal(
                subtotal
        );

        order.setDiscountAmount(
                discountAmount
        );

        order.setCouponCode(
                couponCode
        );

        order.setExpressChargePercentage(
                expressChargePercentage
        );

        order.setExpressChargeAmount(
                expressChargeAmount
        );

        order.setTotalAmount(
                totalAmount
        );

        order.setPickupDate(
                pickupDate
        );

        order.setPickupTime(
                pickupTime
        );

        order.setDeliveryDate(
                request.deliveryDate()
        );

        order.setDeliveryTime(
                request.deliveryTime()
                        .trim()
        );

        order.setHomeDelivery(
                Boolean.TRUE.equals(
                        request.homeDelivery()
                )
        );

        order.setItems(
                orderItems
        );

        Order savedOrder =
                orderRepository.save(
                        order
                );

        return toOrderResponse(
                savedOrder
        );
    }

    private Customer findOrCreateCustomer(
            WalkInOrderRequest.CustomerRequest request
    ) {

        String phone =
                request.phone()
                        .trim();

        return customerRepository
                .findByPhone(
                        phone
                )
                .orElseGet(() -> {

                    if (
                            request.name() == null ||
                            request.name().isBlank()
                    ) {

                        throw new RuntimeException(
                                "Customer name is required for new customer"
                        );
                    }

                    Customer customer =
                            new Customer();

                    customer.setName(
                            request.name()
                                    .trim()
                    );

                    customer.setPhone(
                            phone
                    );

                    customer.setActive(
                            true
                    );

                    return customerRepository
                            .save(
                                    customer
                            );
                });
    }

    private Order.OrderItem buildOrderItem(
            WalkInOrderRequest.OrderItemRequest request
    ) {

        if (
                request.productId() == null
        ) {

            throw new RuntimeException(
                    "Product id is required"
            );
        }

        if (
                request.typeId() == null
        ) {

            throw new RuntimeException(
                    "Product type id is required"
            );
        }

        if (
                request.serviceId() == null
        ) {

            throw new RuntimeException(
                    "Service id is required"
            );
        }

        if (
                request.quantity() == null ||
                request.quantity()
                        .compareTo(
                                BigDecimal.ZERO
                        ) <= 0
        ) {

            throw new RuntimeException(
                    "Quantity must be greater than 0"
            );
        }

        Product product =
                productRepository
                        .findById(
                                request.productId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product not found"
                                )
                        );

        if (
                !product.isActive()
        ) {

            throw new RuntimeException(
                    "Product is not active"
            );
        }

        Product.ProductType productType =
                product.getTypes()
                        .stream()
                        .filter(
                                type ->
                                        type.getId()
                                                .equals(
                                                        request.typeId()
                                                )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product type not found for selected product"
                                )
                        );

        Product.ProductServicePrice service =
                productType.getServices()
                        .stream()
                        .filter(
                                item ->
                                        item.getId()
                                                .equals(
                                                        request.serviceId()
                                                )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Service not found for selected product type"
                                )
                        );

        BigDecimal quantity =
                request.quantity();

        if (
                product.getUnit()
                        == Product.PricingUnit.PC
                        &&
                        quantity.stripTrailingZeros()
                                .scale() > 0
        ) {

            throw new RuntimeException(
                    "Per piece product quantity must be a whole number"
            );
        }

        BigDecimal unitPrice =
                money(
                        service.getPrice()
                );

        BigDecimal lineTotal =
                unitPrice.multiply(
                        quantity
                );

        lineTotal =
                money(
                        lineTotal
                );

        Order.OrderItem orderItem =
                new Order.OrderItem();

        orderItem.setProductId(
                product.getId()
        );

        orderItem.setProductName(
                product.getName()
        );

        orderItem.setProductTypeId(
                productType.getId()
        );

        orderItem.setProductTypeName(
                productType.getName()
        );

        orderItem.setServiceId(
                service.getId()
        );

        orderItem.setServiceName(
                service.getName()
        );

        orderItem.setUnit(
                product.getUnit()
        );

        orderItem.setQuantity(
                quantity
        );

        orderItem.setUnitPrice(
                unitPrice
        );

        orderItem.setLineTotal(
                lineTotal
        );

        return orderItem;
    }

    private BigDecimal calculateDiscount(
            Coupon coupon,
            BigDecimal subtotal
    ) {

        BigDecimal discount;

        if (
                coupon.getDiscountType()
                        == Coupon.DiscountType.PERCENTAGE
        ) {

            discount =
                    subtotal
                            .multiply(
                                    coupon.getDiscountValue()
                            )
                            .divide(
                                    BigDecimal.valueOf(100),
                                    2,
                                    RoundingMode.HALF_UP
                            );

        } else {

            discount =
                    coupon.getDiscountValue();
        }

        if (
                discount.compareTo(
                        subtotal
                ) > 0
        ) {

            discount =
                    subtotal;
        }

        return money(
                discount
        );
    }

    private void validateOrderRequest(
            WalkInOrderRequest request
    ) {

        if (
                request == null
        ) {

            throw new RuntimeException(
                    "Order request is required"
            );
        }

        if (
                request.customer() == null
        ) {

            throw new RuntimeException(
                    "Customer is required"
            );
        }

        if (
                request.customer().phone() == null ||
                request.customer().phone().isBlank()
        ) {

            throw new RuntimeException(
                    "Customer phone is required"
            );
        }

        if (
                request.items() == null ||
                request.items().isEmpty()
        ) {

            throw new RuntimeException(
                    "At least one order item is required"
            );
        }

        if (
                request.deliveryDate() == null
        ) {

            throw new RuntimeException(
                    "Delivery date is required"
            );
        }

        if (
                request.deliveryTime() == null ||
                request.deliveryTime().isBlank()
        ) {

            throw new RuntimeException(
                    "Delivery time is required"
            );
        }

        if (
                request.deliveryDate()
                        .isBefore(
                                LocalDate.now()
                        )
        ) {

            throw new RuntimeException(
                    "Delivery date cannot be before pickup date"
            );
        }
    }

    private BigDecimal money(
            BigDecimal value
    ) {

        if (
                value == null
        ) {

            return BigDecimal.ZERO
                    .setScale(
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private ProductResponse toProductResponse(
            Product product
    ) {

        List<ProductResponse.TypeResponse> types =
                product.getTypes()
                        .stream()
                        .map(type -> {

                            List<ProductResponse.ServiceResponse>
                                    services =
                                    type.getServices()
                                            .stream()
                                            .map(service ->
                                                    new ProductResponse
                                                            .ServiceResponse(
                                                            service.getId(),
                                                            service.getName(),
                                                            service.getPrice()
                                                    )
                                            )
                                            .toList();

                            return new ProductResponse
                                    .TypeResponse(
                                    type.getId(),
                                    type.getName(),
                                    services
                            );
                        })
                        .toList();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getUnit(),
                product.isActive(),
                types
        );
    }

    private CouponResponse toCouponResponse(
            Coupon coupon
    ) {

        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getMinimumOrderAmount(),
                coupon.isActive()
        );
    }

    private ExpressChargeResponse toExpressChargeResponse(
            ExpressCharge expressCharge
    ) {

        return new ExpressChargeResponse(
                expressCharge.getId(),
                expressCharge.getName(),
                expressCharge.getPercentage(),
                expressCharge.isActive()
        );
    }

    private OrderResponse toOrderResponse(
            Order order
    ) {

        OrderResponse.CustomerResponse customerResponse =
                new OrderResponse.CustomerResponse(
                        order.getCustomer()
                                .getId(),
                        order.getCustomer()
                                .getName(),
                        order.getCustomer()
                                .getPhone()
                );

        List<OrderResponse.OrderItemResponse> items =
                order.getItems()
                        .stream()
                        .map(item ->
                                new OrderResponse.OrderItemResponse(
                                        item.getId(),
                                        item.getProductId(),
                                        item.getProductName(),
                                        item.getProductTypeId(),
                                        item.getProductTypeName(),
                                        item.getServiceId(),
                                        item.getServiceName(),
                                        item.getUnit(),
                                        item.getQuantity(),
                                        item.getUnitPrice(),
                                        item.getLineTotal()
                                )
                        )
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                customerResponse,
                items,
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getCouponCode(),
                order.getExpressChargePercentage(),
                order.getExpressChargeAmount(),
                order.getTotalAmount(),
                order.getPickupDate(),
                order.getPickupTime(),
                order.getDeliveryDate(),
                order.getDeliveryTime(),
                order.getStorageLabel(),
                order.isHomeDelivery(),
                order.isSettled(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                "Walk-in order created successfully"
        );
    }
}