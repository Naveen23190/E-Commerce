package com.ecommerce.store.order;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

final class RazorpaySignature {
    private RazorpaySignature() {}

    static boolean isValid(String orderId, String paymentId, String secret, String signature) {
        if (orderId == null || paymentId == null || secret == null || signature == null) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = mac.doFinal((orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8));
            byte[] supplied = HexFormat.of().parseHex(signature);
            return MessageDigest.isEqual(expected, supplied);
        } catch (IllegalArgumentException | java.security.GeneralSecurityException exception) {
            return false;
        }
    }
}
