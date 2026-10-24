package com.ecommerce.store.order;

import com.ecommerce.store.user.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "payment_attempts", uniqueConstraints = @UniqueConstraint(columnNames = "gateway_order_id"))
public class PaymentAttempt {
    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private AppUser customer;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_order_id", unique = true)
    private CustomerOrder customerOrder;
    @Column(name = "gateway_order_id", nullable = false, unique = true, length = 80)
    private String gatewayOrderId;
    @Column(length = 80)
    private String gatewayPaymentId;
    @Column(length = 80)
    private String gatewayRefundId;
    @Column(nullable = false, length = 20)
    private String status = "CREATED";
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee;
    @Column(nullable = false, length = 500)
    private String shippingAddress;
    @Column(nullable = false, length = 6)
    private String pinCode;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Column(nullable = false)
    private Instant expiresAt;
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentAttemptItem> items = new ArrayList<>();

    protected PaymentAttempt() {}

    public PaymentAttempt(AppUser customer, String gatewayOrderId, BigDecimal subtotal,
                          BigDecimal shippingFee, String shippingAddress, String pinCode, Instant expiresAt) {
        this.customer = customer;
        this.gatewayOrderId = gatewayOrderId;
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.shippingAddress = shippingAddress;
        this.pinCode = pinCode;
        this.expiresAt = expiresAt;
    }

    public void add(PaymentAttemptItem item) {
        items.add(item);
        item.setAttempt(this);
    }

    public void complete(String paymentId, CustomerOrder order) {
        this.gatewayPaymentId = paymentId;
        this.customerOrder = order;
        this.status = "COMPLETED";
    }

    public void beginRefund(String paymentId) {
        this.gatewayPaymentId = paymentId;
        this.status = "REFUND_PENDING";
    }

    public void completeRefund(String refundId, String refundStatus) {
        this.gatewayRefundId = refundId;
        this.status = "processed".equalsIgnoreCase(refundStatus) ? "REFUNDED" : "REFUND_PENDING";
    }

    public void requireManualRefund() {
        this.status = "REFUND_REQUIRED";
    }

    public String getId() { return id; }
    public AppUser getCustomer() { return customer; }
    public CustomerOrder getCustomerOrder() { return customerOrder; }
    public String getGatewayOrderId() { return gatewayOrderId; }
    public String getGatewayPaymentId() { return gatewayPaymentId; }
    public String getGatewayRefundId() { return gatewayRefundId; }
    public String getStatus() { return status; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public String getShippingAddress() { return shippingAddress; }
    public String getPinCode() { return pinCode; }
    public Instant getExpiresAt() { return expiresAt; }
    public List<PaymentAttemptItem> getItems() { return items; }
}
