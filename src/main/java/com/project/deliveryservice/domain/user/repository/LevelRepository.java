package com.project.deliveryservice.domain.user.repository;

import com.project.deliveryservice.domain.user.entity.Level;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LevelRepository extends JpaRepository<Level, Long> {

    Optional<Level> findByName(String name);
}
