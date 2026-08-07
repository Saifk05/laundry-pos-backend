package com.laundry.pos.repository;

import com.laundry.pos.model.ProductServicePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductServicePriceRepository
        extends JpaRepository<ProductServicePrice, UUID> {

    List<ProductServicePrice>
    findAllByProduct_IdAndActiveTrue(UUID productId);

    Optional<ProductServicePrice>
    findByProduct_IdAndService_IdAndActiveTrue(
            UUID productId,
            UUID serviceId
    );
}