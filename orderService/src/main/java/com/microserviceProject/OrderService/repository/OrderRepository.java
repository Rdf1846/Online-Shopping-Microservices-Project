package com.microserviceProject.OrderService.repository;

import com.microserviceProject.OrderService.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
