package com.ecommerce.store.order;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

class RazorpaySignatureTest {
    @Test
    void verifiesOnlyTheMatchingOrderAndPaymentSignature() throws Exception {
        String secret = "test-secret";
        String orderId = "order_test_123";
        String paymentId = "pay_test_456";
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = HexFormat.of().formatHex(
                mac.doFinal((orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8)));

        assertTrue(RazorpaySignature.isValid(orderId, paymentId, secret, signature));
        assertFalse(RazorpaySignature.isValid("order_other", paymentId, secret, signature));
        assertFalse(RazorpaySignature.isValid(orderId, paymentId, secret, "not-hex"));
        assertFalse(RazorpaySignature.isValid(orderId, paymentId, secret, null));
    }
}
