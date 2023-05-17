package com.project.deliveryservice.domain.auth.controller;

import com.project.deliveryservice.common.constants.AuthConstants;
import com.project.deliveryservice.domain.auth.dto.LoginRequest;
import com.project.deliveryservice.domain.auth.service.AuthService;
import com.project.deliveryservice.jwt.JwtTokenDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public JwtTokenDto login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/reissue")
    public JwtTokenDto reissue(@RequestHeader(value = AuthConstants.AUTHORIZATION_HEADER) String bearerToken) {
        return authService.reissue(bearerToken);
    }
}
