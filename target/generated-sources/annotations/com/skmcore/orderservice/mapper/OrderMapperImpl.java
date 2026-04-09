package com.skmcore.orderservice.mapper;

import com.skmcore.orderservice.dto.OrderItemResponse;
import com.skmcore.orderservice.dto.OrderResponse;
import com.skmcore.orderservice.model.Order;
import com.skmcore.orderservice.model.OrderItem;
import com.skmcore.orderservice.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-09T22:07:34+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.18 (Microsoft)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderResponse toResponse(Order order) {
        if ( order == null ) {
            return null;
        }

        UUID id = null;
        String orderNumber = null;
        UUID customerId = null;
        OrderStatus status = null;
        BigDecimal totalAmount = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        id = order.getId();
        orderNumber = order.getOrderNumber();
        customerId = order.getCustomerId();
        status = order.getStatus();
        totalAmount = order.getTotalAmount();
        createdAt = order.getCreatedAt();
        updatedAt = order.getUpdatedAt();

        List<OrderItemResponse> items = order.getItems().stream().map(this::toItemResponse).toList();

        OrderResponse orderResponse = new OrderResponse( id, orderNumber, customerId, status, items, totalAmount, createdAt, updatedAt );

        return orderResponse;
    }

    @Override
    public OrderItemResponse toItemResponse(OrderItem item) {
        if ( item == null ) {
            return null;
        }

        UUID id = null;
        UUID productId = null;
        String productName = null;
        int quantity = 0;
        BigDecimal unitPrice = null;
        BigDecimal subtotal = null;

        id = item.getId();
        productId = item.getProductId();
        productName = item.getProductName();
        quantity = item.getQuantity();
        unitPrice = item.getUnitPrice();
        subtotal = item.getSubtotal();

        OrderItemResponse orderItemResponse = new OrderItemResponse( id, productId, productName, quantity, unitPrice, subtotal );

        return orderItemResponse;
    }
}
