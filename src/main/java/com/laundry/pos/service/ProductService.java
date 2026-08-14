package com.laundry.pos.service;

import com.laundry.pos.model.Product;
import com.laundry.pos.repository.ProductRepository;
import com.laundry.pos.request.ProductRequest;
import com.laundry.pos.response.ProductResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(
            ProductRepository productRepository
    ) {
        this.productRepository = productRepository;
    }

    /* =========================================
       CREATE PRODUCT
    ========================================= */

    public ProductResponse createProduct(
            ProductRequest request
    ) {

        validateRequest(request);

        String name =
                request.name()
                        .trim();

        if (
                productRepository
                        .existsByNameIgnoreCase(name)
        ) {
            throw new RuntimeException(
                    "Product already exists"
            );
        }

        Product product =
                new Product();

        product.setName(name);
        product.setUnit(request.unit());
        product.setActive(request.active());

        List<Product.ProductType> types =
                buildTypes(
                        request.types()
                );

        product.setTypes(types);

        Product savedProduct =
                productRepository.save(product);

        return toResponse(savedProduct);
    }

    /* =========================================
       GET ALL PRODUCTS
    ========================================= */

    public ProductResponse.ProductListResponse
    getAllProducts() {

        List<ProductResponse> products =
                productRepository
                        .findAll()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        if (products.isEmpty()) {

            return new ProductResponse
                    .ProductListResponse(
                    "No products available",
                    products
            );
        }

        return new ProductResponse
                .ProductListResponse(
                "Products fetched successfully",
                products
        );
    }

    /* =========================================
       GET PRODUCT BY ID
    ========================================= */

    public ProductResponse getProductById(
            UUID id
    ) {

        Product product =
                productRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product not found"
                                )
                        );

        return toResponse(product);
    }

    /* =========================================
       UPDATE PRODUCT
    ========================================= */

    public ProductResponse updateProduct(
            UUID id,
            ProductRequest request
    ) {

        validateRequest(request);

        Product product =
                productRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product not found"
                                )
                        );

        String name =
                request.name()
                        .trim();

        productRepository
                .findByNameIgnoreCase(name)
                .ifPresent(existingProduct -> {

                    if (
                            !existingProduct
                                    .getId()
                                    .equals(id)
                    ) {
                        throw new RuntimeException(
                                "Product already exists"
                        );
                    }
                });

        product.setName(name);
        product.setUnit(request.unit());
        product.setActive(request.active());

        List<Product.ProductType> types =
                buildTypes(
                        request.types()
                );

        product.setTypes(types);

        Product updatedProduct =
                productRepository.save(product);

        return toResponse(updatedProduct);
    }

    /* =========================================
       DELETE / DEACTIVATE PRODUCT
    ========================================= */

    public void deleteProduct(
            UUID id
    ) {

        Product product =
                productRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product not found"
                                )
                        );

        product.setActive(false);

        productRepository.save(product);
    }

    /* =========================================
       BUILD PRODUCT TYPES
    ========================================= */

    private List<Product.ProductType>
    buildTypes(
            List<ProductRequest.TypeRequest>
                    typeRequests
    ) {

        List<Product.ProductType> types =
                new ArrayList<>();

        for (
                ProductRequest.TypeRequest typeRequest
                : typeRequests
        ) {

            Product.ProductType type =
                    new Product.ProductType();

            type.setName(
                    typeRequest
                            .name()
                            .trim()
            );

            List<Product.ProductServicePrice>
                    services =
                    buildServices(
                            typeRequest.services()
                    );

            type.setServices(services);

            types.add(type);
        }

        return types;
    }

    /* =========================================
       BUILD SERVICES
    ========================================= */

    private List<Product.ProductServicePrice>
    buildServices(
            List<ProductRequest.ServiceRequest>
                    serviceRequests
    ) {

        List<Product.ProductServicePrice> services =
                new ArrayList<>();

        for (
                ProductRequest.ServiceRequest serviceRequest
                : serviceRequests
        ) {

            Product.ProductServicePrice service =
                    new Product.ProductServicePrice();

            service.setName(
                    serviceRequest
                            .name()
                            .trim()
            );

            service.setPrice(
                    serviceRequest.price()
            );

            services.add(service);
        }

        return services;
    }

    /* =========================================
       VALIDATION
    ========================================= */

    private void validateRequest(
            ProductRequest request
    ) {

        if (
                request.name() == null ||
                request.name().isBlank()
        ) {
            throw new RuntimeException(
                    "Product name is required"
            );
        }

        if (
                request.unit() == null
        ) {
            throw new RuntimeException(
                    "Pricing unit is required"
            );
        }

        if (
                request.types() == null ||
                request.types().isEmpty()
        ) {
            throw new RuntimeException(
                    "At least one product type is required"
            );
        }

        for (
                ProductRequest.TypeRequest type
                : request.types()
        ) {

            if (
                    type.name() == null ||
                    type.name().isBlank()
            ) {
                throw new RuntimeException(
                        "Product type name is required"
                );
            }

            if (
                    type.services() == null ||
                    type.services().isEmpty()
            ) {
                throw new RuntimeException(
                        "At least one service is required for product type "
                                + type.name()
                );
            }

            for (
                    ProductRequest.ServiceRequest service
                    : type.services()
            ) {

                if (
                        service.name() == null ||
                        service.name().isBlank()
                ) {
                    throw new RuntimeException(
                            "Service name is required"
                    );
                }

                if (
                        service.price() == null ||
                        service.price()
                                .compareTo(
                                        BigDecimal.ZERO
                                ) < 0
                ) {
                    throw new RuntimeException(
                            "Service price cannot be negative"
                    );
                }
            }
        }
    }

    /* =========================================
       RESPONSE MAPPING
    ========================================= */

    private ProductResponse toResponse(
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
}