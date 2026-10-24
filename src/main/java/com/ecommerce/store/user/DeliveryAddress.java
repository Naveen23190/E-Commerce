package com.ecommerce.store.user;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "delivery_addresses")
public class DeliveryAddress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private AppUser customer;
    @Column(nullable = false, length = 40)
    private String label;
    @Column(nullable = false, length = 500)
    private String address;
    @Column(nullable = false, length = 6)
    private String pinCode;
    @Column(nullable = false)
    private boolean defaultAddress;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected DeliveryAddress() {}

    public DeliveryAddress(AppUser customer, String label, String address, String pinCode, boolean defaultAddress) {
        this.customer = customer;
        update(label, address, pinCode);
        this.defaultAddress = defaultAddress;
    }

    public void update(String label, String address, String pinCode) {
        this.label = label;
        this.address = address;
        this.pinCode = pinCode;
    }

    public void setDefaultAddress(boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    public Long getId() { return id; }
    public AppUser getCustomer() { return customer; }
    public String getLabel() { return label; }
    public String getAddress() { return address; }
    public String getPinCode() { return pinCode; }
    public boolean isDefaultAddress() { return defaultAddress; }
    public Instant getCreatedAt() { return createdAt; }
}
