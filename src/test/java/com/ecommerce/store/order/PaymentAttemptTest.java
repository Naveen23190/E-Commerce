package com.ecommerce.store.order;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecommerce.store.user.AppUser;
import com.ecommerce.store.user.Role;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PaymentAttemptTest {
    @Test
    void recordsRefundRecoveryStates() {
        AppUser customer = new AppUser("Test Customer", "customer@example.com", "encoded", Role.CUSTOMER);
        PaymentAttempt attempt = new PaymentAttempt(customer, "order_test", BigDecimal.TEN,
                BigDecimal.ZERO, "Test address", "560001", Instant.now().plusSeconds(900));

        attempt.beginRefund("pay_test");
        assertEquals("REFUND_PENDING", attempt.getStatus());

        attempt.completeRefund("rfnd_test", "processed");
        assertEquals("REFUNDED", attempt.getStatus());
        assertEquals("rfnd_test", attempt.getGatewayRefundId());

        attempt.beginRefund("pay_test");
        attempt.completeRefund("rfnd_pending", "pending");
        assertEquals("REFUND_PENDING", attempt.getStatus());

        attempt.requireManualRefund();
        assertEquals("REFUND_REQUIRED", attempt.getStatus());
    }
}
