package com.project.deliveryservice.domain.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.project.deliveryservice.common.constants.AuthConstants;
import com.project.deliveryservice.common.entity.Address;
import com.project.deliveryservice.common.exception.ErrorMsg;
import com.project.deliveryservice.domain.auth.dto.LoginRequest;
import com.project.deliveryservice.domain.auth.dto.RegisterRequest;
import com.project.deliveryservice.domain.user.dto.UserInfo;
import com.project.deliveryservice.domain.user.entity.User;
import com.project.deliveryservice.domain.user.repository.UserRepository;
import com.project.deliveryservice.jwt.JwtInvalidException;
import com.project.deliveryservice.jwt.JwtTokenDto;
import com.project.deliveryservice.jwt.JwtTokenProvider;
import com.project.deliveryservice.utils.ApiUtils.ApiResponse;
import com.project.deliveryservice.utils.JsonUtils;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.security.Key;
import java.util.Optional;

import static com.project.deliveryservice.TestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
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
    JsonUtils jsonUtils;
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

    <T> ApiResponse<T> deserializeApiResponse(String json, Class<T> clazz) throws JsonProcessingException {
        JavaType javaType = jsonUtils.getParametricType(ApiResponse.class, clazz);
        return jsonUtils.deserialize(json, javaType);
    }

    String getLoginRequest(String email, String password) throws JsonProcessingException {
        LoginRequest loginRequest = new LoginRequest(email, password);
        return jsonUtils.serialize(loginRequest);
    }

    String getRegisterRequest(String email, String password, String username, String city, String street, String zipCode) throws JsonProcessingException {
        RegisterRequest registerRequest = new RegisterRequest(email, password, username, city, street, zipCode);
        return jsonUtils.serialize(registerRequest);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 정보로 로그인을 요청하면 Forbidden 상태를 반환한다.")
    public void test_01() throws Exception {

        String requestContent = getLoginRequest(testEmail, testPassword);
        when(mockUserRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        mockMvc.perform(
                post("/api/auth/login")
                        .content(requestContent)
                        .contentType("application/json"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("data").value(nullValue()))
                .andExpect(jsonPath("errorMsg").value(testEmail + " is not found"));
    }

    @Test
    @DisplayName("email 이 없으면 400 상태를 반환한다")
    public void test_01_1() throws Exception {

        String requestContent = getLoginRequest(null, testPassword);

        mockMvc.perform(
                        post("/api/auth/login")
                                .content(requestContent)
                                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 요청 시에 email 이 올바른 형태가 아니면 400 상태를 반환한다")
    public void test_01_2() throws Exception {

        String requestContent = getLoginRequest("test", testPassword);

        mockMvc.perform(
                        post("/api/auth/login")
                                .content(requestContent)
                                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 요청 시에 비밀번호가 없으면 400 상태를 반환한다")
    public void test_01_3() throws Exception {

        String requestContent = getLoginRequest(testEmail, null);

        mockMvc.perform(
                        post("/api/auth/login")
                                .content(requestContent)
                                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("일치하지 않는 비밀번호로 로그인 요청을 보내면 Forbidden 상태를 반환한다.")
    public void test_02() throws Exception {

        User user = getTestUser(testEmail, passwordEncoder.encode(testPassword), testAuthority);
        String requestContent = getLoginRequest(testEmail, "12345");
        when(mockUserRepository.findByEmail(testEmail)).thenReturn(Optional.ofNullable(user));

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

        User user = getTestUser(testEmail, passwordEncoder.encode(testPassword), testAuthority);
        String requestContent = getLoginRequest(testEmail, testPassword);
        when(mockUserRepository.findByEmail(testEmail)).thenReturn(Optional.ofNullable(user));

        String accessToken = getTestAccessToken(secretKey);
        String refreshToken = getTestRefreshToken(refreshSecretKey);
        when(mockJwtTokenProvider.createAccessToken(testEmail, testAuthority)).thenReturn(accessToken);
        when(mockJwtTokenProvider.createRefreshToken(testEmail, testAuthority)).thenReturn(refreshToken);
        when(mockJwtTokenProvider.parseClaimsFromRefreshToken(refreshToken)).thenReturn(Jwts.claims().setSubject(testEmail));

        MvcResult mvcResult = mockMvc.perform(
                post("/api/auth/login")
                        .content(requestContent)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("errorMsg").value(nullValue()))
                .andReturn();

        ApiResponse<JwtTokenDto> res = deserializeApiResponse(mvcResult.getResponse().getContentAsString(), JwtTokenDto.class);
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

        String refreshToken = getTestRefreshToken(refreshSecretKey);

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

        User user = getTestUser(testEmail, testPassword, testAuthority);
        when(mockUserRepository.findByEmail(testEmail)).thenReturn(Optional.ofNullable(user));

        String accessToken = getTestAccessToken(secretKey);
        String refreshToken = getTestRefreshToken(refreshSecretKey);
        when(mockJwtTokenProvider.createAccessToken(testEmail, testAuthority)).thenReturn(accessToken);
        when(mockJwtTokenProvider.createRefreshToken(testEmail, testAuthority)).thenReturn(refreshToken);
        when(mockJwtTokenProvider.parseClaimsFromRefreshToken(refreshToken))
                .thenReturn(Jwts.claims().setSubject(testEmail));

        MvcResult mvcResult = mockMvc.perform(
                post("/api/auth/reissue")
                        .header(AuthConstants.AUTHORIZATION_HEADER, AuthConstants.BEARER_PREFIX + refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("errorMsg").value(nullValue()))
                .andReturn();

        ApiResponse<JwtTokenDto> res = deserializeApiResponse(mvcResult.getResponse().getContentAsString(), JwtTokenDto.class);
        assertThat(res.getData().getAccessToken(), equalTo(accessToken));
        assertThat(res.getData().getRefreshToken(), equalTo(refreshToken));
        assertThat(res.getData().getGrantType(), equalTo(AuthConstants.GRANT_TYPE_BEARER));

    }

    @Test
    @DisplayName("토큰 재발급시 accessToken 이 주어지면 Forbidden 상태를 반환한다.")
    public void test_07() throws Exception {

        String accessToken = getTestAccessToken(secretKey);
        User user = getTestUser(testEmail, testPassword, testAuthority);
        when(mockUserRepository.findByEmail(testEmail)).thenReturn(Optional.ofNullable(user));
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

    @Test
    @DisplayName("회원가입 요청 시에 email 이 null 이면 에러 메세지와 400 상태를 반환한다. - Validation Error")
    public void test_08() throws Exception {

        // given
        String request = getRegisterRequest(null, testPassword,
                "123", "seoul", "songpa", "12345");

        // when
        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("data").value(nullValue()))
                .andExpect(jsonPath("errorMsg").value("email must not be empty"))
                .andReturn();
    }

    @Test
    @DisplayName("회원가입 시에 email 이 이메일 형태를 갖지 않으면 400 상태를 반환한다.")
    public void test_08_1() throws Exception {

        String request = getRegisterRequest("test", testPassword,
                "123", "seoul", "songpa", "12345");

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("data").value(nullValue()))
                .andExpect(jsonPath("errorMsg").value("email must be a well-formed email address"))
                .andReturn();
    }

    @Test
    @DisplayName("회원가입 시에 username 이 없으면 400 상태를 반환한다.")
    public void test_08_2() throws Exception {

        String request = getRegisterRequest(testEmail, testPassword,
                null, "seoul", "songpa", "12345");

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("data").value(nullValue()))
                .andExpect(jsonPath("errorMsg").value("username must not be empty"))
                .andReturn();
    }

    @Test
    @DisplayName("회원가입 시에 username 이 너무 짧으면 400 상태를 반환한다.")
    public void test_08_3() throws Exception {

        String request = getRegisterRequest(testEmail, testPassword,
                "12", "seoul", "songpa", "12345");

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("data").value(nullValue()))
                .andExpect(jsonPath("errorMsg").value("username length must be between 3 and 12"))
                .andReturn();
    }

    @Test
    @DisplayName("회원가입 시에 username 이 너무 길면 400 상태를 반환한다.")
    public void test_08_4() throws Exception {

        String request = getRegisterRequest(testEmail, testPassword,
                "123456789101112131415", "seoul", "songpa", "12345");

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("data").value(nullValue()))
                .andExpect(jsonPath("errorMsg").value("username length must be between 3 and 12"))
                .andReturn();
    }

    @Test
    @DisplayName("회원가입 시에 주소 값이 완전하지 않을 경우 400 상태를 반환한다.")
    public void test_08_5() throws Exception {

        String request = getRegisterRequest(testEmail, testPassword,
                "123", "seoul", null, "12345");

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("data").value(nullValue()))
                .andExpect(jsonPath("errorMsg").value("street must not be empty"))
                .andReturn();
    }

    @Test
    @DisplayName("회원가입 시에 주소 값의 zipcode 가 숫자 포맷이 아닐 경우 400 상태를 반환한다.")
    public void test_08_6() throws Exception {

        String request = getRegisterRequest(testEmail, testPassword,
                "123", "seoul",  "songpa", "12345a");

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("data").value(nullValue()))
                .andExpect(jsonPath("errorMsg").value("zipCode must only contain numeric value"))
                .andReturn();
    }

    @Test
    @DisplayName("중복된 email 로 회원가입을 요청하면 에러 메세지와 403 상태를 반환한다.")
    public void test_09() throws Exception {

        // given
        String request = getRegisterRequest(testEmail, testPassword, "test",
                "seoul", "songpa" , "12345");
        User user = getDefaultTestUser(1L, testEmail, new Address("seoul", "songpa", "12345"));

        // when
        when(mockUserRepository.findByEmail(testEmail)).thenReturn(Optional.ofNullable(user));
        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("data").value(nullValue()))
                .andExpect(jsonPath("errorMsg").value(testEmail + ErrorMsg.DUPLICATED))
                .andReturn();
    }

    @Test
    @DisplayName("회원 가입에 성공하면 UserInfoDto 와 201 상태를 반환한다.")
    public void test_10() throws Exception {

        // given
        String request = getRegisterRequest(testEmail, testPassword, "test",
                "seoul", "songpa" , "12345");
        Address address = new Address("seoul", "songpa", "012345");
        User user = getDefaultTestUser(1L, "test@gmail.com", address);

        // when
        when(mockUserRepository.save(any())).thenReturn(user);
        MvcResult mvcResult = mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
                .andExpect(status().isCreated())
                .andReturn();

        ApiResponse<UserInfo> res = deserializeApiResponse(mvcResult.getResponse().getContentAsString(), UserInfo.class);
        assertThat(res.getErrorMsg(), is(nullValue()));
        assertThat(res.getData().getAddress(), equalTo(address.toString()));
        assertThat(res.getData().getEmail(), equalTo("test@gmail.com"));
        assertThat(res.getData().getLevel(), equalTo("고마운분"));
    }
}