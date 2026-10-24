package com.ecommerce.store.product;

import com.ecommerce.store.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "product_reviews", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "customer_id"}))
public class ProductReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(nullable = false)
    private Product product;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "customer_id", nullable = false)
    private AppUser customer;
    @Column(nullable = false)
    private Integer rating;
    @Column(nullable = false, length = 1000)
    private String comment;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ProductReview() {}
    public ProductReview(Product product, AppUser customer, int rating, String comment) {
        this.product = product;
        this.customer = customer;
        this.rating = rating;
        this.comment = comment;
    }
    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public AppUser getCustomer() { return customer; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
}
