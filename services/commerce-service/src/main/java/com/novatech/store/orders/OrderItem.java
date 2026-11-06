package com.novatech.store.orders;

import com.novatech.store.catalog.Product;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    public PurchaseOrder order;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    public Product product;
    @Column(name = "sku_snapshot", nullable = false)
    public String skuSnapshot;
    @Column(name = "name_snapshot", nullable = false)
    public String nameSnapshot;
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    public BigDecimal unitPrice;
    @Column(nullable = false)
    public int quantity;
    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    public BigDecimal lineTotal;
}
