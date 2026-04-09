package com.skmcore.orderservice.service.impl;

import com.skmcore.orderservice.dto.CreateOrderRequest;
import com.skmcore.orderservice.dto.OrderResponse;
import com.skmcore.orderservice.event.OrderEvent;
import com.skmcore.orderservice.exception.EntityNotFoundException;
import com.skmcore.orderservice.mapper.OrderMapper;
import com.skmcore.orderservice.model.Order;
import com.skmcore.orderservice.model.OrderItem;
import com.skmcore.orderservice.model.OrderStatus;
import com.skmcore.orderservice.repository.OrderRepository;
import com.skmcore.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_NUMBER_PREFIX = "ORD";

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 200))
    public OrderResponse createOrder(CreateOrderRequest request) {
        String orderNumber = generateOrderNumber();
        Order order = new Order(orderNumber, request.customerId());

        request.items().forEach(itemReq -> {
            OrderItem item = new OrderItem(
                    itemReq.productId(),
                    itemReq.productName(),
                    itemReq.quantity(),
                    itemReq.unitPrice()
            );
            order.addItem(item);
        });

        Order saved = orderRepository.save(order);
        log.info("Created order orderNumber={} customerId={}", saved.getOrderNumber(), saved.getCustomerId());

        eventPublisher.publishEvent(new OrderEvent(this, saved.getId(), saved.getOrderNumber(), OrderStatus.PENDING));
        return orderMapper.toResponse(saved);
    }

    @Override
    public OrderResponse getOrder(UUID id) {
        return orderMapper.toResponse(findOrderById(id));
    }

    @Override
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderNumber));
        return orderMapper.toResponse(order);
    }

    @Override
    public Page<OrderResponse> getOrdersByCustomer(UUID customerId, Pageable pageable) {
        return orderRepository.findAllByCustomerId(customerId, pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    public Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findAllByStatus(status, pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(UUID id, OrderStatus newStatus) {
        Order order = findOrderById(id);
        OrderStatus previous = order.getStatus();
        order.transitionTo(newStatus);
        Order saved = orderRepository.save(order);

        log.info("Order {} status changed from {} to {}", saved.getOrderNumber(), previous, newStatus);
        eventPublisher.publishEvent(new OrderEvent(this, saved.getId(), saved.getOrderNumber(), newStatus));
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void cancelOrder(UUID id) {
        Order order = findOrderById(id);
        order.transitionTo(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info("Order {} cancelled", order.getOrderNumber());
        eventPublisher.publishEvent(new OrderEvent(this, order.getId(), order.getOrderNumber(), OrderStatus.CANCELLED));
    }

    private Order findOrderById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));
    }

    private String generateOrderNumber() {
        String candidate;
        do {
            candidate = ORDER_NUMBER_PREFIX + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (orderRepository.existsByOrderNumber(candidate));
        return candidate;
    }
}
