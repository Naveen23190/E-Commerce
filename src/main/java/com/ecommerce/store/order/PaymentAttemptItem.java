package com.ecommerce.store.order;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "payment_attempt_items")
public class PaymentAttemptItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private PaymentAttempt attempt;
    @Column(nullable = false)
    private Long productId;
    @Column(nullable = false, length = 140)
    private String productName;
    @Column(nullable = false)
    private Integer quantity;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    protected PaymentAttemptItem() {}

    public PaymentAttemptItem(Long productId, String productName, int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    void setAttempt(PaymentAttempt attempt) { this.attempt = attempt; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}
