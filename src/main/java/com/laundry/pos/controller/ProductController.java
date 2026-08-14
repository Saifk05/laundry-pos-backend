package com.laundry.pos.controller;

import com.laundry.pos.request.ProductRequest;
import com.laundry.pos.response.ProductResponse;
import com.laundry.pos.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(
            ProductService productService
    ) {
        this.productService = productService;
    }

    /* =========================================
       CREATE PRODUCT
    ========================================= */

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @RequestBody ProductRequest request
    ) {

        ProductResponse response =
                productService.createProduct(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /* =========================================
       GET ALL PRODUCTS
    ========================================= */

    @GetMapping
    public ResponseEntity<
            ProductResponse.ProductListResponse
            > getAllProducts() {

        return ResponseEntity.ok(
                productService.getAllProducts()
        );
    }

    /* =========================================
       GET PRODUCT BY ID
    ========================================= */

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                productService.getProductById(id)
        );
    }

    /* =========================================
       UPDATE PRODUCT
    ========================================= */

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID id,
            @RequestBody ProductRequest request
    ) {

        return ResponseEntity.ok(
                productService.updateProduct(
                        id,
                        request
                )
        );
    }

    /* =========================================
       DELETE / DEACTIVATE PRODUCT
    ========================================= */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable UUID id
    ) {

        productService.deleteProduct(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}