package com.ecommerce.store.order;

import com.ecommerce.store.user.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(nullable = false)
    private AppUser customer;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;
    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal;
    @Column(precision = 12, scale = 2)
    private BigDecimal shippingFee;
    @Column(nullable = false, length = 20)
    private String status = "PLACED";
    @Column(nullable = false, length = 500)
    private String shippingAddress;
    @Column(length = 6)
    private String pinCode;
    @Column(length = 24)
    private String paymentMethod;
    @Column(length = 24)
    private String paymentStatus;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    protected CustomerOrder() {}
    public CustomerOrder(AppUser customer, BigDecimal subtotal, BigDecimal shippingFee, String shippingAddress,
                         String pinCode, String paymentMethod, String paymentStatus) {
        this.customer = customer;
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.total = subtotal.add(shippingFee);
        this.shippingAddress = shippingAddress;
        this.pinCode = pinCode;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
    }
    public void add(OrderItem item) { items.add(item); item.setOrder(this); }
    public void updateStatus(OrderStatus next) {
        OrderStatus current = OrderStatus.valueOf(status);
        if (!current.canTransitionTo(next)) throw new IllegalArgumentException("Invalid order status transition");
        status = next.name();
        if (next == OrderStatus.CANCELLED) paymentStatus = "CANCELLED";
    }
    public Long getId() { return id; }
    public AppUser getCustomer() { return customer; }
    public BigDecimal getTotal() { return total; }
    public BigDecimal getSubtotal() { return subtotal == null ? total : subtotal; }
    public BigDecimal getShippingFee() { return shippingFee == null ? BigDecimal.ZERO : shippingFee; }
    public String getStatus() { return status; }
    public String getShippingAddress() { return shippingAddress; }
    public String getPinCode() { return pinCode; }
    public String getPaymentMethod() { return paymentMethod == null ? "LEGACY" : paymentMethod; }
    public String getPaymentStatus() { return paymentStatus == null ? "NOT_RECORDED" : paymentStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
}
