package com.project.deliveryservice.domain.user.service;

import com.project.deliveryservice.domain.user.entity.User;
import com.project.deliveryservice.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserOrThrowById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not exist"));
    }
}
