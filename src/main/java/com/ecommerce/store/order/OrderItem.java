package com.ecommerce.store.order;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(nullable = false)
    private CustomerOrder order;
    @Column(nullable = false, length = 140)
    private String productName;
    @Column(nullable = false)
    private Long productId;
    @Column(nullable = false)
    private Integer quantity;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;
    protected OrderItem() {}
    public OrderItem(Long productId, String productName, int quantity, BigDecimal unitPrice) {
        this.productId = productId; this.productName = productName; this.quantity = quantity; this.unitPrice = unitPrice;
    }
    void setOrder(CustomerOrder order) { this.order = order; }
    public Long getId() { return id; }
    public String getProductName() { return productName; }
    public Long getProductId() { return productId; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}
