package com.laundry.pos.service;

import com.laundry.pos.model.Product;
import com.laundry.pos.repository.ProductRepository;
import com.laundry.pos.repository.ProductServicePriceRepository;
import com.laundry.pos.response.ProductResponse;
import com.laundry.pos.response.ProductServiceResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductServicePriceRepository productServicePriceRepository;

    public ProductService(
            ProductRepository productRepository,
            ProductServicePriceRepository productServicePriceRepository
    ) {
        this.productRepository = productRepository;
        this.productServicePriceRepository = productServicePriceRepository;
    }

    public List<ProductResponse> getProducts() {

        return productRepository
                .findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ProductResponse toResponse(
            Product product
    ) {

        List<ProductServiceResponse> services =
                productServicePriceRepository
                        .findAllByProduct_IdAndActiveTrue(
                                product.getId()
                        )
                        .stream()
                        .filter(price ->
                                price.getService().isActive()
                        )
                        .map(price ->
                                new ProductServiceResponse(
                                        price.getService().getId(),
                                        price.getService().getName(),
                                        price.getPrice()
                                )
                        )
                        .toList();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getIcon(),
                services
        );
    }
}