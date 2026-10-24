package com.ecommerce.store.cart;

import com.ecommerce.store.product.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "cart_items", uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "product_id"}))
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(nullable = false)
    private ShoppingCart cart;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) @JoinColumn(nullable = false)
    private Product product;
    @Column(nullable = false)
    private Integer quantity;
    protected CartItem() {}
    public CartItem(Product product, int quantity) { this.product = product; this.quantity = quantity; }
    void setCart(ShoppingCart cart) { this.cart = cart; }
    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
