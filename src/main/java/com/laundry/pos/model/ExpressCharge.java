package com.laundry.pos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "express_charges",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_express_charge_percentage",
                        columnNames = "percentage"
                )
        }
)
public class ExpressCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            nullable = false,
            length = 100
    )
    private String name;

    @Column(
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal percentage;

    @Column(nullable = false)
    private boolean active = true;

    public ExpressCharge() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}