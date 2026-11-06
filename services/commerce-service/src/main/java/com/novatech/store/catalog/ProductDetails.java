package com.novatech.store.catalog;

import java.util.HashMap;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "product_details")
public class ProductDetails {
    @Id
    public Long productId;
    public String description = "";
    public Map<String, String> specs = new HashMap<>();
    public ProductDetails() {
    }

    public ProductDetails(Long id, String description, Map<String, String> specs) {
        this.productId = id;
        this.description = description;
        this.specs = specs;
    }
}
