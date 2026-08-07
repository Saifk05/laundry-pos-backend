package com.laundry.pos.repository;

import com.laundry.pos.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, UUID> {
}