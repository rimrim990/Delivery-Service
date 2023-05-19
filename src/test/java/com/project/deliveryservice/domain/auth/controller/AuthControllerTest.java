package com.project.deliveryservice.domain.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deliveryservice.common.constants.AuthConstants;
import com.project.deliveryservice.common.exception.ErrorMsg;
import com.project.deliveryservice.domain.auth.dto.LoginRequest;
import com.project.deliveryservice.domain.user.entity.Role;
import com.project.deliveryservice.domain.user.entity.Level;
import com.project.deliveryservice.domain.user.entity.User;
import com.project.deliveryservice.domain.user.repository.UserRepository;
import com.project.deliveryservice.jwt.JwtInvalidException;
import com.project.deliveryservice.jwt.JwtTokenDto;
import com.project.deliveryservice.jwt.JwtTokenProvider;
import com.project.deliveryservice.utils.ApiUtils.ApiResponse;
import com.project.deliveryservice.utils.JwtUtils;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.security.Key;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    @Value("${jwt.secret}")
    String secret;
    @Value("${jwt.refresh-secret}")
    String refreshSecret;

    @MockBean
    JwtTokenProvider mockJwtTokenProvider;
    @MockBean
    UserRepository mockUserRepository;

    private Key secretKey;
    private Key refreshSecretKey;

    @BeforeEach
    public void setup() {
        secretKey = JwtUtils.generateKey(secret);
        refreshSecretKey = JwtUtils.generateKey(refreshSecret);
    }

    User getUser(String email, String password, String authority) {
        Level level = Level.builder()
                .role(Role.valueOf(authority))
                .build();

        return User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .level(level)
                .build();
    }

    String getLoginRequest(String email, String password) throws JsonProcessingException {
        LoginRequest loginRequest = new LoginRequest(email, password);
        return objectMapper.writeValueAsString(loginRequest);
    }

    private String getAccessToken() {
        return JwtUtils.createJwtToken("test", "ROLE_ADMIN", 10, secretKey);
    }

    private String getRefreshToken() {
        return JwtUtils.createJwtToken("test", "ROLE_ADMIN", 30, refreshSecretKey);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 정보로 로그인을 요청하면 Forbidden 상태를 반환한다.")
    public void test_01() throws Exception {

        String requestContent = getLoginRequest("test", "1234");
        when(mockUserRepository.findByEmail("test")).thenReturn(Optional.empty());

        mockMvc.perform(
                post("/api/auth/login")
                        .content(requestContent)
                        .contentType("application/json"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("data").value(nullValue()))
                .andExpect(jsonPath("errorMsg").value("test is not found"));
    }

    @Test
    @DisplayName("일치하지 않는 비밀번호로 로그인 요청을 보내면 Forbidden 상태를 반환한다.")
    public void test_02() throws Exception {

        User user = getUser("test", "1234", "ROLE_ADMIN");
        String requestContent = getLoginRequest("test", "12345");
        when(mockUserRepository.findByEmail("test")).thenReturn(Optional.ofNullable(user));

        mockMvc.perform(
                post("/api/auth/login")
                        .content(requestContent)
                        .contentType("application/json"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("data").value(nullValue()))
                .andExpect(jsonPath("errorMsg").value(ErrorMsg.PASSWORD_NOT_MATCH));
    }

    @Test
    @DisplayName("유효한 로그인 요청이 들어오면 JwtTokenDto 를 반환한다.")
    public void test_03() throws Exception {

        User user = getUser("test", "1234", "ROLE_ADMIN");
        String requestContent = getLoginRequest("test", "1234");
        when(mockUserRepository.findByEmail("test")).thenReturn(Optional.ofNullable(user));

        String accessToken = getAccessToken();
        String refreshToken = getRefreshToken();
        when(mockJwtTokenProvider.createAccessToken("test", "ROLE_ADMIN")).thenReturn(accessToken);
        when(mockJwtTokenProvider.createRefreshToken("test", "ROLE_ADMIN")).thenReturn(refreshToken);
        when(mockJwtTokenProvider.parseClaimsFromRefreshToken(refreshToken)).thenReturn(Jwts.claims().setSubject("test"));

        MvcResult mvcResult = mockMvc.perform(
                post("/api/auth/login")
                        .content(requestContent)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("errorMsg").value(nullValue()))
                .andReturn();

        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, JwtTokenDto.class);
        ApiResponse<JwtTokenDto> res = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), javaType);
        assertThat(res.getData().getAccessToken(), equalTo(accessToken));
        assertThat(res.getData().getRefreshToken(), equalTo(refreshToken));
        assertThat(res.getData().getGrantType(), equalTo(AuthConstants.GRANT_TYPE_BEARER));
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
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("data").value(nullValue()))
                .andExpect(jsonPath("errorMsg").value(ErrorMsg.INVALID_GRANT_TYPE));
    }

    @Test
    @DisplayName("유효한 refreshToken 을 가지고 토큰 재발급을 요청하면 jwtTokenDto 를 반환한다.")
    public void test_06() throws Exception {

        User user = getUser("test", "1234", "ROLE_ADMIN");
        when(mockUserRepository.findByEmail("test")).thenReturn(Optional.ofNullable(user));

        String accessToken = getAccessToken();
        String refreshToken = getRefreshToken();
        when(mockJwtTokenProvider.createAccessToken("test", "ROLE_ADMIN")).thenReturn(accessToken);
        when(mockJwtTokenProvider.createRefreshToken("test", "ROLE_ADMIN")).thenReturn(refreshToken);
        when(mockJwtTokenProvider.parseClaimsFromRefreshToken(refreshToken))
                .thenReturn(Jwts.claims().setSubject("test"));

        MvcResult mvcResult = mockMvc.perform(
                post("/api/auth/reissue")
                        .header(AuthConstants.AUTHORIZATION_HEADER, AuthConstants.BEARER_PREFIX + refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("errorMsg").value(nullValue()))
                .andReturn();

        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, JwtTokenDto.class);
        ApiResponse<JwtTokenDto> res = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), javaType);
        assertThat(res.getData().getAccessToken(), equalTo(accessToken));
        assertThat(res.getData().getRefreshToken(), equalTo(refreshToken));
        assertThat(res.getData().getGrantType(), equalTo(AuthConstants.GRANT_TYPE_BEARER));

    }

    @Test
    @DisplayName("토큰 재발급시 accessToken 이 주어지면 Forbidden 상태를 반환한다.")
    public void test_07() throws Exception {

        String accessToken = getAccessToken();
        User user = getUser("test", "1234", "ROLE_ADMIN");
        when(mockUserRepository.findByEmail("test")).thenReturn(Optional.ofNullable(user));
        when(mockJwtTokenProvider.parseClaimsFromRefreshToken(accessToken))
                .thenThrow(new JwtInvalidException(ErrorMsg.DIFFERENT_SIGNATURE_KEY));

        mockMvc.perform(
                        post("/api/auth/reissue")
                                .header(AuthConstants.AUTHORIZATION_HEADER, AuthConstants.BEARER_PREFIX + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("data").value(nullValue()))
                .andExpect(jsonPath("errorMsg").value(ErrorMsg.DIFFERENT_SIGNATURE_KEY))
                .andReturn();
    }
}