package com.project.deliveryservice.domain.order.repository;

import com.project.deliveryservice.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
