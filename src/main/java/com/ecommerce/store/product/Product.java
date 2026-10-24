package com.ecommerce.store.product;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 140)
    private String name;
    @Column(nullable = false, length = 2000)
    private String description;
    @Column(nullable = false, length = 80)
    private String category;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
    @Column(nullable = false)
    private Integer stock;
    @Column(length = 500)
    private String imageUrl;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Product() {}
    public Product(String name, String description, String category, BigDecimal price, Integer stock, String imageUrl) {
        this.name = name; this.description = description; this.category = category;
        this.price = price; this.stock = stock; this.imageUrl = imageUrl;
    }
    public void update(String name, String description, String category, BigDecimal price, Integer stock, String imageUrl) {
        this.name = name; this.description = description; this.category = category;
        this.price = price; this.stock = stock; this.imageUrl = imageUrl;
    }
    public void reduceStock(int quantity) { this.stock -= quantity; }
    public void increaseStock(int quantity) { this.stock += quantity; }
    public void setStock(int stock) { this.stock = stock; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
    public String getImageUrl() { return imageUrl; }
    public Instant getCreatedAt() { return createdAt; }
}
