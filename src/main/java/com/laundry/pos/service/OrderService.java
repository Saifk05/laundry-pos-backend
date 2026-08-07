package com.laundry.pos.service;

import com.laundry.pos.model.*;
import com.laundry.pos.repository.*;
import com.laundry.pos.request.WalkInOrderRequest;
import com.laundry.pos.response.OrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductServicePriceRepository productServicePriceRepository;
    private final CouponRepository couponRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CustomerRepository customerRepository,
            ProductServicePriceRepository productServicePriceRepository,
            CouponRepository couponRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
        this.productServicePriceRepository = productServicePriceRepository;
        this.couponRepository = couponRepository;
    }

    @Transactional
    public OrderResponse createWalkInOrder(
            WalkInOrderRequest request
    ) {

        if (
                request.customerPhone() == null ||
                request.customerPhone().isBlank()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Customer phone is required"
            );
        }

        if (
                request.customerName() == null ||
                request.customerName().isBlank()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Customer name is required"
            );
        }

        if (
                request.items() == null ||
                request.items().isEmpty()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order must contain at least one item"
            );
        }

        Customer customer =
                customerRepository
                        .findByPhoneAndActiveTrue(
                                request.customerPhone()
                        )
                        .orElseGet(() -> {

                            Customer newCustomer =
                                    new Customer();

                            newCustomer.setName(
                                    request.customerName()
                            );

                            newCustomer.setPhone(
                                    request.customerPhone()
                            );

                            newCustomer.setActive(true);

                            return customerRepository.save(
                                    newCustomer
                            );
                        });

        int subtotal = 0;
        int totalPieces = 0;

        List<OrderItem> orderItems =
                new ArrayList<>();

        Order order = new Order();

        for (WalkInOrderRequest.Item itemRequest
                : request.items()) {

            if (
                    itemRequest.quantity() == null ||
                    itemRequest.quantity() <= 0
            ) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Quantity must be greater than 0"
                );
            }

            ProductServicePrice price =
                    productServicePriceRepository
                            .findByProduct_IdAndService_IdAndActiveTrue(
                                    itemRequest.productId(),
                                    itemRequest.serviceId()
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.BAD_REQUEST,
                                            "Invalid product and service combination"
                                    )
                            );

            int itemTotal =
                    price.getPrice() *
                    itemRequest.quantity();

            subtotal += itemTotal;

            totalPieces +=
                    itemRequest.quantity();

            OrderItem orderItem =
                    new OrderItem();

            orderItem.setOrder(order);

            orderItem.setProduct(
                    price.getProduct()
            );

            orderItem.setService(
                    price.getService()
            );

            orderItem.setProductName(
                    price.getProduct().getName()
            );

            orderItem.setServiceName(
                    price.getService().getName()
            );

            orderItem.setQuantity(
                    itemRequest.quantity()
            );

            orderItem.setUnitPrice(
                    price.getPrice()
            );

            orderItem.setTotalPrice(
                    itemTotal
            );

            orderItems.add(orderItem);
        }

        int couponDiscount = 0;
        String couponCode = null;

        if (
                request.couponCode() != null &&
                !request.couponCode().isBlank()
        ) {

            Coupon coupon =
                    couponRepository
                            .findByCodeIgnoreCase(
                                    request.couponCode()
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.BAD_REQUEST,
                                            "Invalid coupon"
                                    )
                            );

            if (!coupon.isActive()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Coupon is inactive"
                );
            }

            couponCode =
                    coupon.getCode();

            couponDiscount =
                    coupon.getAmount();
        }

        int expressCharge =
                request.expressDelivery()
                        ? 100
                        : 0;

        int totalAmount =
                subtotal
                - couponDiscount
                + expressCharge;

        if (totalAmount < 0) {
            totalAmount = 0;
        }

        order.setOrderNumber(
                generateOrderNumber()
        );

        order.setCustomer(customer);

        order.setDeliveryDate(
                request.deliveryDate()
        );

        order.setDeliverySlot(
                request.deliverySlot()
        );

        order.setHomeDelivery(
                request.homeDelivery()
        );

        order.setExpressDelivery(
                request.expressDelivery()
        );

        order.setWashingArea(
                request.washingArea()
        );

        order.setPressingArea(
                request.pressingArea()
        );

        order.setCouponCode(
                couponCode
        );

        order.setSubtotal(
                subtotal
        );

        order.setCouponDiscount(
                couponDiscount
        );

        order.setExpressCharge(
                expressCharge
        );

        order.setTotalPieces(
                totalPieces
        );

        order.setTotalAmount(
                totalAmount
        );

        order.setStatus(
                "PROCESSING"
        );

        Order savedOrder =
                orderRepository.save(order);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(savedOrder);
        }

        orderItemRepository.saveAll(
                orderItems
        );

        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                customer.getName(),
                customer.getPhone(),
                totalPieces,
                subtotal,
                couponDiscount,
                expressCharge,
                totalAmount,
                savedOrder.getStatus(),
                "Order created successfully"
        );
    }

    private String generateOrderNumber() {

        long number =
                System.currentTimeMillis()
                % 10_000_000;

        return String.format(
                "%07d",
                number
        );
    }
}