package com.project.deliveryservice.domain.user.service;

import com.project.deliveryservice.common.exception.DuplicatedArgumentException;
import com.project.deliveryservice.common.exception.ErrorMsg;
import com.project.deliveryservice.domain.user.entity.User;
import com.project.deliveryservice.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserOrThrowById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not exist"));
    }

    public User getUserOrThrowByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email + " is not found"));
    }

    public void throwIfUserExistByEmail(String email) {
        userRepository.findByEmail(email)
                .ifPresent(u -> {
                    throw new DuplicatedArgumentException(u.getEmail() + ErrorMsg.DUPLICATED);
                });
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
