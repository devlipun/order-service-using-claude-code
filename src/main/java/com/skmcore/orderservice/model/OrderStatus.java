package com.skmcore.orderservice.model;

/**
 * Order lifecycle states with embedded transition rules (state-machine logic
 * lives on the entity, not in the service layer).
 */
public enum OrderStatus {

    PENDING {
        @Override public boolean canTransitionTo(OrderStatus next) {
            return next == CONFIRMED || next == CANCELLED;
        }
    },
    CONFIRMED {
        @Override public boolean canTransitionTo(OrderStatus next) {
            return next == PROCESSING || next == CANCELLED;
        }
    },
    PROCESSING {
        @Override public boolean canTransitionTo(OrderStatus next) {
            return next == SHIPPED || next == CANCELLED;
        }
    },
    SHIPPED {
        @Override public boolean canTransitionTo(OrderStatus next) {
            return next == DELIVERED;
        }
    },
    DELIVERED {
        @Override public boolean canTransitionTo(OrderStatus next) {
            return false;
        }
    },
    CANCELLED {
        @Override public boolean canTransitionTo(OrderStatus next) {
            return false;
        }
    };

    public abstract boolean canTransitionTo(OrderStatus next);
}
