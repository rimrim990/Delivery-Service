package com.project.deliveryservice.domain.auth.service;

import com.project.deliveryservice.common.constants.AuthConstants;
import com.project.deliveryservice.domain.auth.dto.LoginRequest;
import com.project.deliveryservice.domain.user.entity.Grade;
import com.project.deliveryservice.domain.user.entity.Level;
import com.project.deliveryservice.domain.user.entity.User;
import com.project.deliveryservice.domain.user.repository.UserRepository;
import com.project.deliveryservice.jwt.JwtInvalidException;
import com.project.deliveryservice.jwt.JwtTokenDto;
import com.project.deliveryservice.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    UserRepository mockUserRepository;
    PasswordEncoder passwordEncoder;
    JwtTokenProvider mockJwtProvider;

    AuthService authService;

    @BeforeEach
    public void setup() {
        mockUserRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        mockJwtProvider = Mockito.mock(JwtTokenProvider.class);
        authService = new AuthService(mockUserRepository, mockJwtProvider, passwordEncoder);
    }

    User getUser(String email, String password, String authority) {
        Level level = Level.builder()
                .grade(Grade.valueOf(authority))
                .build();

        return User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .level(level)
                .build();
    }

    @Test
    @DisplayName("로그인 시 사용자 이름이 존재하지 않으면 UsernameNotFoundException 던진다.")
    public void givenNotExistUserName_whenLogin_thenThrowUsernameNotFoundException() {

        LoginRequest loginRequest = new LoginRequest("test", "1234");

        Throwable throwable = assertThrows(UsernameNotFoundException.class, () -> authService.login(loginRequest));

        assertThat(throwable, isA(UsernameNotFoundException.class));
        assertThat(throwable.getMessage(), equalTo("test is not found"));
    }

    @Test
    @DisplayName("로그인 요청 시 비밀번호가 존재하지 읂여먼 BadCredentialsException 던진다.")
    public void givenNotMatchedPassword_whenLogin_thenThrowBadCredentialsException() {

        LoginRequest loginRequest = new LoginRequest("test", "1234");
        when(mockUserRepository.findByEmail("test")).thenReturn(
                Optional.of(
                        getUser("test", "12345", "ADMIN")
                )
        );

        Throwable throwable = assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));

        assertThat(throwable, isA(BadCredentialsException.class));
        assertThat(throwable.getMessage(), equalTo("password is not match"));
    }

    @Test
    @DisplayName("유효한 로그인 요청이 들어오면 JwtTokenDto 를 반환한다.")
    public void givenValidLoginRequest_whenLogin_thenReturnJwtTokenDto() {

        LoginRequest loginRequest = new LoginRequest("test", "1234");
        User user = getUser("test", "1234", "ADMIN");
        when(mockUserRepository.findByEmail("test")).thenReturn(Optional.of(user));
        when(mockJwtProvider.createAccessToken("test", "ADMIN")).thenReturn("accessToken");
        when(mockJwtProvider.createRefreshToken("test", "ADMIN")).thenReturn("refreshToken");

        JwtTokenDto jwtTokenDto = authService.login(loginRequest);

        assertThat(jwtTokenDto.getGrantType(), equalTo(AuthConstants.GRANT_TYPE_BEARER));
        assertThat(jwtTokenDto.getAccessToken(), equalTo("accessToken"));
        assertThat(jwtTokenDto.getRefreshToken(), equalTo("refreshToken"));
    }

    @Test
    @DisplayName("토큰 재발급시 인증타입이 유효하지 않으면 JwtInvalidException 을 던진다.")
    public void givenInvalidGrandType_whenReissue_thenThrowJwtInvalidException() {

        Throwable throwable = assertThrows(JwtInvalidException.class, () -> authService.reissue("refreshToken"));
        assertThat(throwable.getMessage(), equalTo("invalid grant type"));
    }

    @Test
    @DisplayName("토큰 재발급시 claim 이 null 이면 JwtInvalidException 을 던진다.")
    public void givenNullClaims_whenReissue_thenThrowJwtInvalidException() {

        when(mockJwtProvider.parseClaimsFromJwtToken("refreshToken")).thenReturn(null);

        Throwable throwable = assertThrows(JwtInvalidException.class, () -> authService.reissue("Bearer refreshToken"));
        assertThat(throwable.getMessage(), equalTo("claim not exist in token"));
    }

    @Test
    @DisplayName("토큰 재발급시 유효한 refreshToken 이 주어지면 JwtTokenDto 를 반환한다.")
    public void givenValidRefreshToken_whenReissue_thenJwtTokenDto() {

        User user = getUser("test", "1234", "ADMIN");
        Claims claims = Jwts.claims().setSubject("test");
        claims.put(AuthConstants.KEY_ROLES, Collections.singleton("ADMIN"));

        when(mockUserRepository.findByEmail("test")).thenReturn(Optional.of(user));
        when(mockJwtProvider.parseClaimsFromJwtToken("refreshToken")).thenReturn(claims);
        when(mockJwtProvider.createAccessToken("test", "ADMIN")).thenReturn("accessToken");
        when(mockJwtProvider.createRefreshToken("test", "ADMIN")).thenReturn("refreshToken");

        JwtTokenDto jwtTokenDto = authService.reissue("Bearer refreshToken");

        assertThat(jwtTokenDto.getGrantType(), equalTo(AuthConstants.GRANT_TYPE_BEARER));
        assertThat(jwtTokenDto.getAccessToken(), equalTo("accessToken"));
        assertThat(jwtTokenDto.getRefreshToken(), equalTo("refreshToken"));
    }
}