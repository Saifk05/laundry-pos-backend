package com.laundry.pos.controller;

import com.laundry.pos.response.ProductResponse;
import com.laundry.pos.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(
            ProductService productService
    ) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts() {

        return ResponseEntity.ok(
                productService.getProducts()
        );
    }
}