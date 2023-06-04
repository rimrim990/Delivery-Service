package com.project.deliveryservice.domain.item.repository;

import com.project.deliveryservice.domain.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
