package com.project.deliveryservice.domain.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deliveryservice.common.constants.AuthConstants;
import com.project.deliveryservice.domain.auth.dto.LoginRequest;
import com.project.deliveryservice.domain.user.entity.Grade;
import com.project.deliveryservice.domain.user.entity.Level;
import com.project.deliveryservice.domain.user.entity.User;
import com.project.deliveryservice.domain.user.repository.UserRepository;
import com.project.deliveryservice.jwt.JwtTokenDto;
import com.project.deliveryservice.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest()
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    ObjectMapper objectMapper;

    @SpyBean
    JwtTokenProvider spyJwtTokenProvider;
    @SpyBean
    UserRepository spyUserRepository;

    private static final int ONE_SECONDS = 1000;
    private static final int ONE_MINUTE = 60 * ONE_SECONDS;

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

    private String createToken(String email, List<String> roles, Date now, int expireMin, Key key) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put(AuthConstants.KEY_ROLES, roles);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ONE_MINUTE * expireMin))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    String getLoginRequest(String email, String password) throws JsonProcessingException {
        LoginRequest loginRequest = new LoginRequest(email, password);
        return objectMapper.writeValueAsString(loginRequest);
    }

    private String getAccessToken() {
        String secret = "jwtaccesstokenrandomcustomkeybytes";
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return createToken("test", Collections.singletonList("ADMIN"), new Date(), 10, key);
    }

    private String getRefreshToken() {
        String secret = "jwtaccesstokenrandomcustomkeybytes";
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return createToken("test", Collections.singletonList("ADMIN"), new Date(), 30, key);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 정보로 로그인을 요청하면 Forbidden 상태를 반환한다.")
    public void test_01() throws Exception {

        String requestContent = getLoginRequest("test", "1234");
        when(spyUserRepository.findByEmail("test")).thenReturn(Optional.empty());

        mockMvc.perform(
                post("/api/auth/login")
                        .content(requestContent)
                        .contentType("application/json"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("일치하지 않는 비밀번호로 로그인 요청을 보내면 Forbidden 상태를 반환한다.")
    public void test_02() throws Exception {

        User user = getUser("test", "1234", "ADMIN");
        String requestContent = getLoginRequest("test", "12345");
        when(spyUserRepository.findByEmail("test")).thenReturn(Optional.ofNullable(user));

        mockMvc.perform(
                post("/api/auth/login")
                        .content(requestContent)
                        .contentType("application/json"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("유효한 로그인 요청이 들어오면 JwtTokenDto 를 반환한다.")
    public void test_03() throws Exception {

        User user = getUser("test", "1234", "ADMIN");
        String requestContent = getLoginRequest("test", "1234");
        when(spyUserRepository.findByEmail("test")).thenReturn(Optional.ofNullable(user));

        String accessToken = getAccessToken();
        String refreshToken = getRefreshToken();
        when(spyJwtTokenProvider.createAccessToken("email", "ADMIN")).thenReturn(accessToken);
        when(spyJwtTokenProvider.createRefreshToken("email", "ADMIN")).thenReturn(refreshToken);

        MvcResult mvcResult = mockMvc.perform(
                post("/api/auth/login")
                        .content(requestContent)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        JwtTokenDto jwtTokenDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), JwtTokenDto.class);
        assertThat(jwtTokenDto.getAccessToken(), equalTo(accessToken));
        assertThat(jwtTokenDto.getRefreshToken(), equalTo(refreshToken));
        assertThat(jwtTokenDto.getGrantType(), equalTo(AuthConstants.GRANT_TYPE_BEARER));
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 토큰 재발급을 요청하면 BadRequest 상태를 반환한다.")
    public void test_04() throws Exception {

        mockMvc.perform(
                post("/api/auth/reissue")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("토근 재발급시 헤더 토큰이 Bearer 타입이 아니면 Forbidden 상태를 반환한다.")
    public void test_05() throws Exception {

        String refreshToken = getRefreshToken();

        mockMvc.perform(
                        post("/api/auth/reissue")
                                .header(AuthConstants.AUTHORIZATION_HEADER, refreshToken)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void test_06() throws Exception {

        User user = getUser("test", "1234", "ADMIN");
        when(spyUserRepository.findByEmail("test")).thenReturn(Optional.ofNullable(user));

        String accessToken = getAccessToken();
        String refreshToken = getRefreshToken();

        MvcResult mvcResult = mockMvc.perform(
                post("/api/auth/reissue")
                        .header(AuthConstants.AUTHORIZATION_HEADER, AuthConstants.BEARER_PREFIX + refreshToken))
                .andExpect(status().isOk())
                .andReturn();

        JwtTokenDto jwtTokenDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), JwtTokenDto.class);
        assertThat(jwtTokenDto.getAccessToken(), equalTo(accessToken));
        assertThat(jwtTokenDto.getRefreshToken(), equalTo(refreshToken));
        assertThat(jwtTokenDto.getGrantType(), equalTo(AuthConstants.GRANT_TYPE_BEARER));

    }
}