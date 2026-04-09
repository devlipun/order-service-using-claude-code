package com.skmcore.orderservice.dto;

import com.skmcore.orderservice.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        UUID customerId,
        OrderStatus status,
        List<OrderItemResponse> items,
        BigDecimal totalAmount,
        Instant createdAt,
        Instant updatedAt
) {}
