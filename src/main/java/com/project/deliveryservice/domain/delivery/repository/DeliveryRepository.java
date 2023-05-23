package com.project.deliveryservice.domain.delivery.repository;

import com.project.deliveryservice.domain.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}
