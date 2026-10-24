package com.ecommerce.store.order;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OrderStatusTest {
    @Test
    void fulfillmentMustAdvanceOneStepAtATime() {
        assertTrue(OrderStatus.PLACED.canTransitionTo(OrderStatus.CONFIRMED));
        assertFalse(OrderStatus.PLACED.canTransitionTo(OrderStatus.PACKED));
        assertTrue(OrderStatus.OUT_FOR_DELIVERY.canTransitionTo(OrderStatus.DELIVERED));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    void cancellationIsAllowedOnlyBeforePacking() {
        assertTrue(OrderStatus.PLACED.canTransitionTo(OrderStatus.CANCELLED));
        assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.PACKED.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.CONFIRMED));
        assertFalse(OrderStatus.PLACED.canTransitionTo(null));
    }
}
