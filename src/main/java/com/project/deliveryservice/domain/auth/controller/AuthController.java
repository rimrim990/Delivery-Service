package com.project.deliveryservice.domain.auth.controller;

import com.project.deliveryservice.common.constants.AuthConstants;
import com.project.deliveryservice.domain.auth.dto.LoginRequest;
import com.project.deliveryservice.domain.auth.service.AuthService;
import com.project.deliveryservice.domain.auth.dto.RegisterRequest;
import com.project.deliveryservice.domain.user.dto.UserInfoDto;
import com.project.deliveryservice.jwt.JwtTokenDto;

import com.project.deliveryservice.utils.ApiUtils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.project.deliveryservice.utils.ApiUtils.success;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<JwtTokenDto> login(@RequestBody LoginRequest request) {
        log.info("login requested with email " + request.getEmail());
        return success(authService.login(request));
    }

    @PostMapping("/reissue")
    public ApiResponse<JwtTokenDto> reissue(@RequestHeader(value = AuthConstants.AUTHORIZATION_HEADER) String bearerToken) {
        log.info("reissue requested with refreshToken " + bearerToken);
        return success(authService.reissue(bearerToken));
    }

    @PostMapping(value = "/register", produces = "application/json; charset=UTF-8")
    public ResponseEntity<ApiResponse<UserInfoDto>> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success(authService.register(request)));
    }
}
