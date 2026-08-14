package com.laundry.pos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private OrderStatus status =
            OrderStatus.CREATED;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt =
            LocalDateTime.now();

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


    public UUID getId() {
        return id;
    }


    public void setId(UUID id) {
        this.id = id;
    }


    public Customer getCustomer() {
        return customer;
    }


    public void setCustomer(Customer customer) {
        this.customer = customer;
    }


    public BigDecimal getSubtotal() {
        return subtotal;
    }


    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
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


    public OrderStatus getStatus() {
        return status;
    }


    public void setStatus(
            OrderStatus status
    ) {
        this.status = status;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt = createdAt;
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


    public enum OrderStatus {
        CREATED,
        PROCESSING,
        READY,
        COMPLETED,
        CANCELLED
    }


    @Entity
    @Table(name = "order_items")
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
                nullable = false,
                length = 10
        )
        private Product.PricingUnit unit;

        @Column(
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


        public void setId(UUID id) {
            this.id = id;
        }


        public Order getOrder() {
            return order;
        }


        public void setOrder(Order order) {
            this.order = order;
        }


        public UUID getProductId() {
            return productId;
        }


        public void setProductId(
                UUID productId
        ) {
            this.productId = productId;
        }


        public String getProductName() {
            return productName;
        }


        public void setProductName(
                String productName
        ) {
            this.productName = productName;
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
            this.serviceId = serviceId;
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
            this.unit = unit;
        }


        public BigDecimal getQuantity() {
            return quantity;
        }


        public void setQuantity(
                BigDecimal quantity
        ) {
            this.quantity = quantity;
        }


        public BigDecimal getUnitPrice() {
            return unitPrice;
        }


        public void setUnitPrice(
                BigDecimal unitPrice
        ) {
            this.unitPrice = unitPrice;
        }


        public BigDecimal getLineTotal() {
            return lineTotal;
        }


        public void setLineTotal(
                BigDecimal lineTotal
        ) {
            this.lineTotal = lineTotal;
        }
    }
}