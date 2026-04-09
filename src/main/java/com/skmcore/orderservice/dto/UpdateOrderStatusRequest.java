package com.skmcore.orderservice.dto;

import com.skmcore.orderservice.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(

        @NotNull(message = "status is required")
        OrderStatus status
) {}
