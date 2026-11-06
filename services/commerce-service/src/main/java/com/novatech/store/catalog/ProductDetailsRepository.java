package com.novatech.store.catalog;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductDetailsRepository extends MongoRepository<ProductDetails, Long> {
}
