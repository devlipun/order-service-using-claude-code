package com.skmcore.orderservice.service;

import com.skmcore.orderservice.dto.CreateOrderRequest;
import com.skmcore.orderservice.dto.OrderResponse;
import com.skmcore.orderservice.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrder(UUID id);

    OrderResponse getOrderByNumber(String orderNumber);

    Page<OrderResponse> getOrdersByCustomer(UUID customerId, Pageable pageable);

    Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable);

    OrderResponse updateStatus(UUID id, OrderStatus newStatus);

    void cancelOrder(UUID id);
}
