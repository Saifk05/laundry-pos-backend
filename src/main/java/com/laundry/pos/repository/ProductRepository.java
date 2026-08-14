package com.laundry.pos.repository;

import com.laundry.pos.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository
        extends JpaRepository<Product, UUID> {

    boolean existsByNameIgnoreCase(
            String name
    );

    Optional<Product> findByNameIgnoreCase(
            String name
    );

    List<Product> findAllByActiveTrue();
}