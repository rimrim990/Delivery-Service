package com.project.deliveryservice.domain.user.repository;

import com.project.deliveryservice.domain.user.entity.Level;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LevelRepository extends JpaRepository<Level, Long> {
}
