package com.ecommerce.store.order;

import java.util.List;

public enum OrderStatus {
    PLACED,
    CONFIRMED,
    PACKED,
    SHIPPED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;

    private static final List<OrderStatus> FULFILLMENT =
            List.of(PLACED, CONFIRMED, PACKED, SHIPPED, OUT_FOR_DELIVERY, DELIVERED);

    public boolean canTransitionTo(OrderStatus next) {
        if (next == null) return false;
        if (next == CANCELLED) return this == PLACED || this == CONFIRMED;
        if (this == CANCELLED || this == DELIVERED) return false;
        return FULFILLMENT.indexOf(next) == FULFILLMENT.indexOf(this) + 1;
    }
}
