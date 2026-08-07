package com.laundry.pos.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "product_service_prices")
public class ProductServicePrice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private Product product;

    @ManyToOne
    @JoinColumn(
            name = "service_id",
            nullable = false
    )
    private LaundryService service;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private boolean active = true;

    public ProductServicePrice() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public LaundryService getService() {
        return service;
    }

    public void setService(LaundryService service) {
        this.service = service;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}