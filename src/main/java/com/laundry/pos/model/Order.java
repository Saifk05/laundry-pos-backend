package com.laundry.pos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_order_number",
                        columnNames = "order_number"
                )
        }
)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "order_number",
            nullable = false,
            unique = true,
            length = 30
    )
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "customer_id",
            nullable = false
    )
    private Customer customer;

    @Column(
            name = "subtotal",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal subtotal;

    @Column(
            name = "discount_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal discountAmount =
            BigDecimal.ZERO;

    @Column(
            name = "express_charge_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal expressChargeAmount =
            BigDecimal.ZERO;

    @Column(
            name = "total_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal totalAmount;

    @Column(
            name = "paid_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal paidAmount =
            BigDecimal.ZERO;

    @Column(
            name = "balance_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal balanceAmount =
            BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_status",
            nullable = false,
            length = 30
    )
    private PaymentStatus paymentStatus =
            PaymentStatus.PENDING;

    @Column(
            name = "coupon_code",
            length = 50
    )
    private String couponCode;

    @Column(
            name = "express_charge_percentage",
            precision = 5,
            scale = 2
    )
    private BigDecimal expressChargePercentage;

    @Column(
            name = "pickup_date"
    )
    private LocalDate pickupDate;

    @Column(
            name = "pickup_time",
            length = 50
    )
    private String pickupTime;

    @Column(
            name = "delivery_date"
    )
    private LocalDate deliveryDate;

    @Column(
            name = "delivery_time",
            length = 50
    )
    private String deliveryTime;

    @Column(
            name = "storage_label",
            length = 150
    )
    private String storageLabel;

    @Column(
            name = "home_delivery",
            nullable = false
    )
    private boolean homeDelivery = false;

    @Column(
            name = "settled",
            nullable = false
    )
    private boolean settled = false;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 40
    )
    private OrderStatus status =
            OrderStatus.NEW_ORDER;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @Column(
            name = "delivered_at"
    )
    private LocalDateTime deliveredAt;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private List<OrderItem> items =
            new ArrayList<>();


    public Order() {
    }


    @PrePersist
    public void prePersist() {

        LocalDateTime now =
                LocalDateTime.now();

        if (this.createdAt == null) {
            this.createdAt = now;
        }

        if (this.paidAmount == null) {
            this.paidAmount =
                    BigDecimal.ZERO;
        }

        if (this.balanceAmount == null) {

            this.balanceAmount =
                    this.totalAmount != null
                            ? this.totalAmount
                            : BigDecimal.ZERO;
        }

        if (this.paymentStatus == null) {
            this.paymentStatus =
                    PaymentStatus.PENDING;
        }

        this.updatedAt = now;
    }


    @PreUpdate
    public void preUpdate() {

        this.updatedAt =
                LocalDateTime.now();
    }


    public UUID getId() {
        return id;
    }


    public void setId(
            UUID id
    ) {
        this.id = id;
    }


    public String getOrderNumber() {
        return orderNumber;
    }


    public void setOrderNumber(
            String orderNumber
    ) {
        this.orderNumber =
                orderNumber;
    }


    public Customer getCustomer() {
        return customer;
    }


    public void setCustomer(
            Customer customer
    ) {
        this.customer =
                customer;
    }


    public BigDecimal getSubtotal() {
        return subtotal;
    }


    public void setSubtotal(
            BigDecimal subtotal
    ) {
        this.subtotal =
                subtotal;
    }


    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }


    public void setDiscountAmount(
            BigDecimal discountAmount
    ) {
        this.discountAmount =
                discountAmount;
    }


    public BigDecimal getExpressChargeAmount() {
        return expressChargeAmount;
    }


    public void setExpressChargeAmount(
            BigDecimal expressChargeAmount
    ) {
        this.expressChargeAmount =
                expressChargeAmount;
    }


    public BigDecimal getTotalAmount() {
        return totalAmount;
    }


    public void setTotalAmount(
            BigDecimal totalAmount
    ) {
        this.totalAmount =
                totalAmount;
    }


    public BigDecimal getPaidAmount() {
        return paidAmount;
    }


    public void setPaidAmount(
            BigDecimal paidAmount
    ) {
        this.paidAmount =
                paidAmount;
    }


    public BigDecimal getBalanceAmount() {
        return balanceAmount;
    }


    public void setBalanceAmount(
            BigDecimal balanceAmount
    ) {
        this.balanceAmount =
                balanceAmount;
    }


    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }


    public void setPaymentStatus(
            PaymentStatus paymentStatus
    ) {
        this.paymentStatus =
                paymentStatus;
    }


    public String getCouponCode() {
        return couponCode;
    }


    public void setCouponCode(
            String couponCode
    ) {
        this.couponCode =
                couponCode;
    }


    public BigDecimal getExpressChargePercentage() {
        return expressChargePercentage;
    }


    public void setExpressChargePercentage(
            BigDecimal expressChargePercentage
    ) {
        this.expressChargePercentage =
                expressChargePercentage;
    }


    public LocalDate getPickupDate() {
        return pickupDate;
    }


    public void setPickupDate(
            LocalDate pickupDate
    ) {
        this.pickupDate =
                pickupDate;
    }


    public String getPickupTime() {
        return pickupTime;
    }


    public void setPickupTime(
            String pickupTime
    ) {
        this.pickupTime =
                pickupTime;
    }


    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }


    public void setDeliveryDate(
            LocalDate deliveryDate
    ) {
        this.deliveryDate =
                deliveryDate;
    }


    public String getDeliveryTime() {
        return deliveryTime;
    }


    public void setDeliveryTime(
            String deliveryTime
    ) {
        this.deliveryTime =
                deliveryTime;
    }


    public String getStorageLabel() {
        return storageLabel;
    }


    public void setStorageLabel(
            String storageLabel
    ) {
        this.storageLabel =
                storageLabel;
    }


    public boolean isHomeDelivery() {
        return homeDelivery;
    }


    public void setHomeDelivery(
            boolean homeDelivery
    ) {
        this.homeDelivery =
                homeDelivery;
    }


    public boolean isSettled() {
        return settled;
    }


    public void setSettled(
            boolean settled
    ) {
        this.settled =
                settled;
    }


    public OrderStatus getStatus() {
        return status;
    }


    public void setStatus(
            OrderStatus status
    ) {
        this.status =
                status;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt =
                createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    public void setUpdatedAt(
            LocalDateTime updatedAt
    ) {
        this.updatedAt =
                updatedAt;
    }


    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }


    public void setDeliveredAt(
            LocalDateTime deliveredAt
    ) {
        this.deliveredAt =
                deliveredAt;
    }


    public List<OrderItem> getItems() {
        return items;
    }


    public void setItems(
            List<OrderItem> items
    ) {

        this.items.clear();

        if (items != null) {

            for (OrderItem item : items) {
                addItem(item);
            }
        }
    }


    public void addItem(
            OrderItem item
    ) {

        item.setOrder(this);

        this.items.add(item);
    }


    public void removeItem(
            OrderItem item
    ) {

        this.items.remove(item);

        item.setOrder(null);
    }


    public enum PaymentStatus {

        PENDING,

        PARTIALLY_PAID,

        SETTLED
    }


    public enum OrderStatus {

        NEW_ORDER,

        PROCESSING_AT_STORE,

        READY_ORDER,

        DELIVERED,

        CANCELLED
    }


    @Entity
    @Table(
            name = "order_items"
    )
    public static class OrderItem {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(
                name = "order_id",
                nullable = false
        )
        private Order order;

        @Column(
                name = "product_id",
                nullable = false
        )
        private UUID productId;

        @Column(
                name = "product_name",
                nullable = false,
                length = 100
        )
        private String productName;

        @Column(
                name = "product_type_id",
                nullable = false
        )
        private UUID productTypeId;

        @Column(
                name = "product_type_name",
                nullable = false,
                length = 100
        )
        private String productTypeName;

        @Column(
                name = "service_id",
                nullable = false
        )
        private UUID serviceId;

        @Column(
                name = "service_name",
                nullable = false,
                length = 100
        )
        private String serviceName;

        @Enumerated(EnumType.STRING)
        @Column(
                name = "unit",
                nullable = false,
                length = 10
        )
        private Product.PricingUnit unit;

        @Column(
                name = "quantity",
                nullable = false,
                precision = 10,
                scale = 2
        )
        private BigDecimal quantity;

        @Column(
                name = "unit_price",
                nullable = false,
                precision = 10,
                scale = 2
        )
        private BigDecimal unitPrice;

        @Column(
                name = "line_total",
                nullable = false,
                precision = 12,
                scale = 2
        )
        private BigDecimal lineTotal;


        public OrderItem() {
        }


        public UUID getId() {
            return id;
        }


        public void setId(
                UUID id
        ) {
            this.id = id;
        }


        public Order getOrder() {
            return order;
        }


        public void setOrder(
                Order order
        ) {
            this.order =
                    order;
        }


        public UUID getProductId() {
            return productId;
        }


        public void setProductId(
                UUID productId
        ) {
            this.productId =
                    productId;
        }


        public String getProductName() {
            return productName;
        }


        public void setProductName(
                String productName
        ) {
            this.productName =
                    productName;
        }


        public UUID getProductTypeId() {
            return productTypeId;
        }


        public void setProductTypeId(
                UUID productTypeId
        ) {
            this.productTypeId =
                    productTypeId;
        }


        public String getProductTypeName() {
            return productTypeName;
        }


        public void setProductTypeName(
                String productTypeName
        ) {
            this.productTypeName =
                    productTypeName;
        }


        public UUID getServiceId() {
            return serviceId;
        }


        public void setServiceId(
                UUID serviceId
        ) {
            this.serviceId =
                    serviceId;
        }


        public String getServiceName() {
            return serviceName;
        }


        public void setServiceName(
                String serviceName
        ) {
            this.serviceName =
                    serviceName;
        }


        public Product.PricingUnit getUnit() {
            return unit;
        }


        public void setUnit(
                Product.PricingUnit unit
        ) {
            this.unit =
                    unit;
        }


        public BigDecimal getQuantity() {
            return quantity;
        }


        public void setQuantity(
                BigDecimal quantity
        ) {
            this.quantity =
                    quantity;
        }


        public BigDecimal getUnitPrice() {
            return unitPrice;
        }


        public void setUnitPrice(
                BigDecimal unitPrice
        ) {
            this.unitPrice =
                    unitPrice;
        }


        public BigDecimal getLineTotal() {
            return lineTotal;
        }


        public void setLineTotal(
                BigDecimal lineTotal
        ) {
            this.lineTotal =
                    lineTotal;
        }
    }
}