package com.laundry.pos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "payments"
)
public class Payment {

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
            name = "amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_method",
            nullable = false,
            length = 30
    )
    private PaymentMethod paymentMethod;

    @Column(
            name = "reference_number",
            length = 150
    )
    private String referenceNumber;

    @Column(
            name = "paid_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime paidAt;


    public Payment() {
    }


    @PrePersist
    public void prePersist() {

        if (
                this.paidAt == null
        ) {

            this.paidAt =
                    LocalDateTime.now();
        }
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


    public BigDecimal getAmount() {
        return amount;
    }


    public void setAmount(
            BigDecimal amount
    ) {
        this.amount =
                amount;
    }


    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }


    public void setPaymentMethod(
            PaymentMethod paymentMethod
    ) {
        this.paymentMethod =
                paymentMethod;
    }


    public String getReferenceNumber() {
        return referenceNumber;
    }


    public void setReferenceNumber(
            String referenceNumber
    ) {
        this.referenceNumber =
                referenceNumber;
    }


    public LocalDateTime getPaidAt() {
        return paidAt;
    }


    public void setPaidAt(
            LocalDateTime paidAt
    ) {
        this.paidAt =
                paidAt;
    }


    public enum PaymentMethod {

        CASH,

        UPI,

        CARD,

        OTHER
    }
}