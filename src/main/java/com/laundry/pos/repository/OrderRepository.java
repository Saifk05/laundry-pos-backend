package com.laundry.pos.repository;

import com.laundry.pos.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository
        extends JpaRepository<Order, UUID> {
}