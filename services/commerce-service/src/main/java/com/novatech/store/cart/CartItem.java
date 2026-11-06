package com.novatech.store.cart;

import com.novatech.store.catalog.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    public Cart cart;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    public Product product;
    @Column(nullable = false)
    public int quantity;
}
