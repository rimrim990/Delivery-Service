package com.project.deliveryservice.domain.order.repository;

import com.project.deliveryservice.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
