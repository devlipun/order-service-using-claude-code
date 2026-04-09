package com.skmcore.orderservice.service;

import com.skmcore.orderservice.dto.CreateOrderRequest;
import com.skmcore.orderservice.dto.OrderItemRequest;
import com.skmcore.orderservice.dto.OrderResponse;
import com.skmcore.orderservice.exception.EntityNotFoundException;
import com.skmcore.orderservice.exception.InvalidOrderStateException;
import com.skmcore.orderservice.mapper.OrderMapper;
import com.skmcore.orderservice.model.Order;
import com.skmcore.orderservice.model.OrderStatus;
import com.skmcore.orderservice.repository.OrderRepository;
import com.skmcore.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID customerId;
    private CreateOrderRequest createRequest;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        createRequest = new CreateOrderRequest(
                customerId,
                List.of(new OrderItemRequest(
                        UUID.randomUUID(),
                        "Widget Pro",
                        2,
                        new BigDecimal("49.99")
                ))
        );
    }

    @Test
    @DisplayName("createOrder — persists order and publishes event")
    void createOrder_persistsAndPublishesEvent() {
        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(stubResponse());

        OrderResponse result = orderService.createOrder(createRequest);

        assertThat(result).isNotNull();
        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    @DisplayName("getOrder — throws EntityNotFoundException when order missing")
    void getOrder_throwsWhenNotFound() {
        UUID missingId = UUID.randomUUID();
        when(orderRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(missingId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(missingId.toString());
    }

    @Test
    @DisplayName("updateStatus — valid transition succeeds")
    void updateStatus_validTransitionSucceeds() {
        Order order = new Order("ORD-TEST01", customerId);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(stubResponse());

        OrderResponse result = orderService.updateStatus(UUID.randomUUID(), OrderStatus.CONFIRMED);

        assertThat(result).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("updateStatus — invalid transition throws InvalidOrderStateException")
    void updateStatus_invalidTransitionThrows() {
        Order order = new Order("ORD-TEST02", customerId);
        // Move to CONFIRMED first so that PENDING -> SHIPPED is invalid
        order.transitionTo(OrderStatus.CONFIRMED);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(UUID.randomUUID(), OrderStatus.DELIVERED))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("cancelOrder — cancels a PENDING order successfully")
    void cancelOrder_cancelsPendingOrder() {
        Order order = new Order("ORD-TEST03", customerId);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.cancelOrder(UUID.randomUUID());

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(eventPublisher).publishEvent(any());
    }

    private OrderResponse stubResponse() {
        return new OrderResponse(UUID.randomUUID(), "ORD-STUB", customerId,
                OrderStatus.PENDING, List.of(), BigDecimal.ZERO, null, null);
    }
}
