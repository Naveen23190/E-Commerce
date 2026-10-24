package com.ecommerce.store.cart;

import com.ecommerce.store.user.AppUser;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shopping_carts")
public class ShoppingCart {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false) @JoinColumn(nullable = false, unique = true)
    private AppUser user;
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
    protected ShoppingCart() {}
    public ShoppingCart(AppUser user) { this.user = user; }
    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public List<CartItem> getItems() { return items; }
    public void add(CartItem item) { items.add(item); item.setCart(this); }
    public void remove(CartItem item) { items.remove(item); item.setCart(null); }
    public void clear() { items.clear(); }
}
