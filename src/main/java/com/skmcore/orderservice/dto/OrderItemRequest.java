package com.skmcore.orderservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemRequest(

        @NotNull(message = "productId is required")
        UUID productId,

        @NotBlank(message = "productName is required")
        String productName,

        @Min(value = 1, message = "quantity must be at least 1")
        int quantity,

        @NotNull(message = "unitPrice is required")
        @DecimalMin(value = "0.00", inclusive = false, message = "unitPrice must be positive")
        BigDecimal unitPrice
) {}
