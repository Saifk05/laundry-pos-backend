package com.laundry.pos.repository;

import com.laundry.pos.model.ExpressCharge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpressChargeRepository
        extends JpaRepository<ExpressCharge, UUID> {

    boolean existsByPercentage(
            BigDecimal percentage
    );

    Optional<ExpressCharge> findByPercentage(
            BigDecimal percentage
    );

    List<ExpressCharge> findAllByActiveTrue();
}