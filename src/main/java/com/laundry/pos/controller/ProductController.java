package com.laundry.pos.controller;

import com.laundry.pos.request.ProductRequest;
import com.laundry.pos.response.BulkProductResponse;
import com.laundry.pos.response.ProductResponse;
import com.laundry.pos.service.ProductService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;


    public ProductController(
            ProductService productService
    ) {

        this.productService =
                productService;
    }


    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @RequestBody ProductRequest request
    ) {

        ProductResponse response =
                productService
                        .createProduct(
                                request
                        );


        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        response
                );
    }


    @PostMapping(
            value = "/bulk/pdf",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<BulkProductResponse> bulkUploadProductsPdf(
            @RequestParam("file")
            MultipartFile file
    ) {

        BulkProductResponse response =
                productService
                        .bulkUploadProductsPdf(
                                file
                        );


        return ResponseEntity.ok(
                response
        );
    }


    @GetMapping
    public ResponseEntity<
            ProductResponse.ProductListResponse
            > getAllProducts() {

        return ResponseEntity.ok(
                productService
                        .getAllProducts()
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                productService
                        .getProductById(
                                id
                        )
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID id,
            @RequestBody ProductRequest request
    ) {

        return ResponseEntity.ok(
                productService
                        .updateProduct(
                                id,
                                request
                        )
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable UUID id
    ) {

        productService
                .deleteProduct(
                        id
                );


        return ResponseEntity
                .noContent()
                .build();
    }
}