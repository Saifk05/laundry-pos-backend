package com.laundry.pos.repository;

import com.laundry.pos.model.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository
        extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(
            String orderNumber
    );

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findAllByStatusOrderByCreatedAtDesc(
            Order.OrderStatus status
    );

    List<Order> findAllByCustomer_PhoneContainingOrderByCreatedAtDesc(
            String phone
    );

    List<Order> findAllByOrderNumberContainingIgnoreCaseOrderByCreatedAtDesc(
            String orderNumber
    );

    @Query(
            value =
                    "SELECT nextval('laundry_order_number_seq')",
            nativeQuery = true
    )
    Long getNextOrderSequence();

    @Query("""
            SELECT o
            FROM Order o
            WHERE
                (:status IS NULL OR o.status = :status)
                AND (
                    :search IS NULL
                    OR :search = ''
                    OR LOWER(o.orderNumber)
                        LIKE LOWER(CONCAT('%', :search, '%'))
                    OR o.customer.phone
                        LIKE CONCAT('%', :search, '%')
                    OR LOWER(o.customer.name)
                        LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(COALESCE(o.storageLabel, ''))
                        LIKE LOWER(CONCAT('%', :search, '%'))
                )
            ORDER BY o.createdAt DESC
            """)
    List<Order> searchOrders(
            @Param("status")
            Order.OrderStatus status,

            @Param("search")
            String search
    );
}