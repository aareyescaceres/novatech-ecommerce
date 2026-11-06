package com.novatech.store.catalog;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import java.math.BigDecimal;

@Entity
@DynamicUpdate
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false, unique = true, length = 80)
    public String sku;
    @Column(nullable = false, length = 200)
    public String name;
    @Column(nullable = false, length = 100)
    public String brand;
    @Column(nullable = false, length = 60)
    public String category;
    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal price;
    @Column(nullable = false)
    public int stock;
    @Column(nullable = false)
    public boolean active = true;
}
