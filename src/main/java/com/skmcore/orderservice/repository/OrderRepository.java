package com.skmcore.orderservice.repository;

import com.skmcore.orderservice.model.Order;
import com.skmcore.orderservice.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findAllByCustomerId(UUID customerId, Pageable pageable);

    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);

    boolean existsByOrderNumber(String orderNumber);
}
