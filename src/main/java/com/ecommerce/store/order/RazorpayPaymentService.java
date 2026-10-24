package com.ecommerce.store.order;

import com.ecommerce.store.common.ApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class RazorpayPaymentService {
    private static final Logger log = LoggerFactory.getLogger(RazorpayPaymentService.class);

    public record GatewayOrder(@JsonProperty("id") String id,
                               @JsonProperty("amount") long amount,
                               @JsonProperty("currency") String currency) {}
    public record GatewayPayment(@JsonProperty("id") String id,
                                 @JsonProperty("order_id") String orderId,
                                 @JsonProperty("amount") long amount,
                                 @JsonProperty("currency") String currency,
                                 @JsonProperty("status") String status) {}
    public record GatewayRefund(@JsonProperty("id") String id,
                                @JsonProperty("amount") long amount,
                                @JsonProperty("status") String status) {}

    private final RestClient client;
    private final String keyId;
    private final String keySecret;

    public RazorpayPaymentService(@Value("${app.payment.razorpay.key-id:}") String keyId,
                                  @Value("${app.payment.razorpay.key-secret:}") String keySecret) {
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.client = RestClient.builder()
                .baseUrl("https://api.razorpay.com/v1")
                .defaultHeaders(headers -> headers.setBasicAuth(keyId, keySecret))
                .build();
    }

    public String keyId() {
        requireTestCredentials();
        return keyId;
    }

    public GatewayOrder createOrder(long amount, String receipt) {
        requireTestCredentials();
        try {
            GatewayOrder result = client.post().uri("/orders")
                    .body(Map.of("amount", amount, "currency", "INR", "receipt", receipt))
                    .retrieve().body(GatewayOrder.class);
            if (result == null || result.id() == null || result.amount() != amount || !"INR".equals(result.currency()))
                throw new IllegalStateException("Razorpay returned an unexpected order response");
            return result;
        } catch (RestClientException exception) {
            log.warn("Razorpay test order creation failed: {}", exception.getMessage());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to start the Razorpay test checkout. Please try again.");
        }
    }

    public GatewayPayment payment(String paymentId) {
        requireTestCredentials();
        try {
            GatewayPayment result = client.get().uri("/payments/{id}", paymentId)
                    .retrieve().body(GatewayPayment.class);
            if (result == null) throw new IllegalStateException("Razorpay returned an empty payment response");
            return result;
        } catch (RestClientException exception) {
            log.warn("Razorpay test payment verification failed: {}", exception.getMessage());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to verify the Razorpay test payment. Please retry.");
        }
    }

    public GatewayRefund refund(String paymentId, long amount, String attemptId) {
        requireTestCredentials();
        try {
            GatewayRefund result = client.post().uri("/payments/{id}/refund", paymentId)
                    .body(Map.of("amount", amount, "notes", Map.of("ecart_attempt_id", attemptId)))
                    .retrieve().body(GatewayRefund.class);
            if (result == null || result.id() == null || result.amount() != amount
                    || !("processed".equalsIgnoreCase(result.status())
                    || "pending".equalsIgnoreCase(result.status())))
                throw new IllegalStateException("Razorpay returned an unexpected refund response");
            return result;
        } catch (RestClientException exception) {
            log.warn("Razorpay test refund request failed for payment {}: {}", paymentId, exception.getMessage());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Razorpay could not confirm the test refund request.");
        }
    }

    public boolean isValidSignature(String orderId, String paymentId, String signature) {
        requireTestCredentials();
        return RazorpaySignature.isValid(orderId, paymentId, keySecret, signature);
    }

    private void requireTestCredentials() {
        if (keyId.isBlank() || keySecret.isBlank() || !keyId.startsWith("rzp_test_"))
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Razorpay test mode is not configured. Set test-mode Razorpay credentials in the backend environment.");
    }
}
