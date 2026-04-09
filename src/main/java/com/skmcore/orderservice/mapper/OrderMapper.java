package com.skmcore.orderservice.mapper;

import com.skmcore.orderservice.dto.OrderItemResponse;
import com.skmcore.orderservice.dto.OrderResponse;
import com.skmcore.orderservice.model.Order;
import com.skmcore.orderservice.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface OrderMapper {

    @Mapping(target = "items", expression = "java(order.getItems().stream().map(this::toItemResponse).toList())")
    OrderResponse toResponse(Order order);

    OrderItemResponse toItemResponse(OrderItem item);
}
