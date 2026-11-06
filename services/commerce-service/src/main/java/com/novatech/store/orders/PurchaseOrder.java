package com.novatech.store.orders;

import com.novatech.store.user.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public AppUser user;
    @Column(name = "checkout_key", nullable = false, length = 100)
    public String checkoutKey;
    @Column(nullable = false, length = 25)
    public String status;
    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal total;
    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
    @Column(name = "expires_at", nullable = false)
    public Instant expiresAt;
    @Column(name = "paid_at")
    public Instant paidAt;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<OrderItem> items = new ArrayList<>();
}
