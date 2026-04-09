package com.skmcore.orderservice.event;

import com.skmcore.orderservice.model.OrderStatus;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Published whenever an order's status changes. Listeners can react to
 * specific statuses (e.g. send confirmation email on CONFIRMED, trigger
 * fulfilment pipeline on PROCESSING).
 */
public class OrderEvent extends ApplicationEvent {

    private final UUID orderId;
    private final String orderNumber;
    private final OrderStatus status;

    public OrderEvent(Object source, UUID orderId, String orderNumber, OrderStatus status) {
        super(source);
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.status = status;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
