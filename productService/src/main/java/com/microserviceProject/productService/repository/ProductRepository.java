package com.microserviceProject.productService.repository;

import com.microserviceProject.productService.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {

}
