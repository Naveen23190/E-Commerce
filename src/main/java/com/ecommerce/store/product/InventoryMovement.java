package com.ecommerce.store.product;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "inventory_movements")
public class InventoryMovement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long productId;
    @Column(nullable = false, length = 140)
    private String productName;
    @Column(nullable = false)
    private Integer quantityChange;
    @Column(nullable = false)
    private Integer stockAfter;
    @Column(nullable = false, length = 200)
    private String reason;
    @Column(nullable = false, length = 190)
    private String actor;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected InventoryMovement() {}

    public InventoryMovement(Product product, int quantityChange, String reason, String actor) {
        this.productId = product.getId();
        this.productName = product.getName();
        this.quantityChange = quantityChange;
        this.stockAfter = product.getStock();
        this.reason = reason;
        this.actor = actor;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Integer getQuantityChange() { return quantityChange; }
    public Integer getStockAfter() { return stockAfter; }
    public String getReason() { return reason; }
    public String getActor() { return actor; }
    public Instant getCreatedAt() { return createdAt; }
}
