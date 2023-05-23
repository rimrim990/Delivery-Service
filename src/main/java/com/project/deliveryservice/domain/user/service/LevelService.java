package com.project.deliveryservice.domain.user.service;

import com.project.deliveryservice.domain.user.entity.Level;
import com.project.deliveryservice.domain.user.entity.Role;
import com.project.deliveryservice.domain.user.repository.LevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LevelService {

    private final LevelRepository levelRepository;

    public Level getLevelOrThrowByRole(Role role) {
        return levelRepository.findByRole(role)
                .orElseThrow(() -> new RuntimeException("internal server error"));
    }
}
