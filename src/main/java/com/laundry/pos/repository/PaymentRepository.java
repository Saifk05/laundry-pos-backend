package com.laundry.pos.repository;

import com.laundry.pos.model.Payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository
        extends JpaRepository<Payment, UUID> {

    List<Payment>
    findAllByOrder_IdOrderByPaidAtDesc(
            UUID orderId
    );

    List<Payment>
    findAllByPaidAtBetweenOrderByPaidAtDesc(
            LocalDateTime fromDateTime,
            LocalDateTime toDateTime
    );
}