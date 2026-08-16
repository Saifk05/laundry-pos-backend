package com.laundry.pos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_name",
                        columnNames = "name"
                )
        }
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            nullable = false,
            unique = true,
            length = 100
    )
    private String name;

    @Column(
            length = 50
    )
    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 10
    )
    private PricingUnit unit;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private List<ProductType> types =
            new ArrayList<>();


    public Product() {
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


    public String getIcon() {
        return icon;
    }


    public void setIcon(String icon) {
        this.icon = icon;
    }


    public PricingUnit getUnit() {
        return unit;
    }


    public void setUnit(PricingUnit unit) {
        this.unit = unit;
    }


    public boolean isActive() {
        return active;
    }


    public void setActive(boolean active) {
        this.active = active;
    }


    public List<ProductType> getTypes() {
        return types;
    }


    public void setTypes(
            List<ProductType> types
    ) {

        this.types.clear();

        if (types != null) {

            for (ProductType type : types) {
                addType(type);
            }
        }
    }


    public void addType(
            ProductType type
    ) {

        type.setProduct(this);

        this.types.add(type);
    }


    public void removeType(
            ProductType type
    ) {

        this.types.remove(type);

        type.setProduct(null);
    }


    public enum PricingUnit {
        PC,
        KG
    }


    /* =========================================
       PRODUCT TYPE / VARIANT
    ========================================= */

    @Entity
    @Table(
            name = "product_types",
            uniqueConstraints = {
                    @UniqueConstraint(
                            name = "uk_product_type_name",
                            columnNames = {
                                    "product_id",
                                    "name"
                            }
                    )
            }
    )
    public static class ProductType {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @Column(
                nullable = false,
                length = 100
        )
        private String name;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(
                name = "product_id",
                nullable = false
        )
        private Product product;

        @OneToMany(
                mappedBy = "productType",
                cascade = CascadeType.ALL,
                orphanRemoval = true,
                fetch = FetchType.EAGER
        )
        private List<ProductServicePrice> services =
                new ArrayList<>();


        public ProductType() {
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


        public Product getProduct() {
            return product;
        }


        public void setProduct(Product product) {
            this.product = product;
        }


        public List<ProductServicePrice> getServices() {
            return services;
        }


        public void setServices(
                List<ProductServicePrice> services
        ) {

            this.services.clear();

            if (services != null) {

                for (
                        ProductServicePrice service
                        : services
                ) {
                    addService(service);
                }
            }
        }


        public void addService(
                ProductServicePrice service
        ) {

            service.setProductType(this);

            this.services.add(service);
        }


        public void removeService(
                ProductServicePrice service
        ) {

            this.services.remove(service);

            service.setProductType(null);
        }
    }


    /* =========================================
       SERVICE + DYNAMIC PRICE
    ========================================= */

    @Entity
    @Table(
            name = "product_service_prices",
            uniqueConstraints = {
                    @UniqueConstraint(
                            name = "uk_product_type_service_name",
                            columnNames = {
                                    "product_type_id",
                                    "name"
                            }
                    )
            }
    )
    public static class ProductServicePrice {

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
                precision = 10,
                scale = 2
        )
        private BigDecimal price;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(
                name = "product_type_id",
                nullable = false
        )
        private ProductType productType;


        public ProductServicePrice() {
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


        public BigDecimal getPrice() {
            return price;
        }


        public void setPrice(BigDecimal price) {
            this.price = price;
        }


        public ProductType getProductType() {
            return productType;
        }


        public void setProductType(
                ProductType productType
        ) {
            this.productType = productType;
        }
    }
}