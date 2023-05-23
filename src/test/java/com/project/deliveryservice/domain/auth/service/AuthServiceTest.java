package com.project.deliveryservice.domain.auth.service;

import com.project.deliveryservice.common.constants.AuthConstants;
import com.project.deliveryservice.common.entity.Address;
import com.project.deliveryservice.common.exception.DuplicatedArgumentException;
import com.project.deliveryservice.common.exception.ErrorMsg;
import com.project.deliveryservice.domain.auth.dto.LoginRequest;
import com.project.deliveryservice.domain.auth.dto.RegisterRequest;
import com.project.deliveryservice.domain.user.dto.UserInfoDto;
import com.project.deliveryservice.domain.user.entity.Level;
import com.project.deliveryservice.domain.user.entity.User;
import com.project.deliveryservice.domain.user.service.LevelService;
import com.project.deliveryservice.domain.user.service.UserService;
import com.project.deliveryservice.jwt.JwtInvalidException;
import com.project.deliveryservice.jwt.JwtTokenDto;
import com.project.deliveryservice.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static com.project.deliveryservice.TestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class AuthServiceTest {

    UserService mockUserService;
    LevelService mockLevelService;

    PasswordEncoder passwordEncoder;
    JwtTokenProvider mockJwtProvider;

    AuthService authService;

    @BeforeEach
    public void setup() {
        mockUserService = Mockito.mock(UserService.class);
        mockLevelService = Mockito.mock(LevelService.class);
        passwordEncoder = new BCryptPasswordEncoder();
        mockJwtProvider = Mockito.mock(JwtTokenProvider.class);
        authService = new AuthService(mockUserService, mockLevelService, mockJwtProvider, passwordEncoder);
    }

    @Test
    @DisplayName("로그인 시 사용자 이름이 존재하지 않으면 UsernameNotFoundException 던진다.")
    public void test_01() {

        LoginRequest loginRequest = new LoginRequest(testEmail, testPassword);
        when(mockUserService.getUserOrThrowByEmail(testEmail))
                .thenThrow(new UsernameNotFoundException(testEmail + " is not found"));

        Throwable throwable = assertThrows(UsernameNotFoundException.class, () -> authService.login(loginRequest));

        assertThat(throwable.getMessage(), equalTo(testEmail + " is not found"));
    }

    @Test
    @DisplayName("로그인 요청 시 비밀번호가 존재하지 않으면 BadCredentialsException 던진다.")
    public void test_02() {

        LoginRequest loginRequest = new LoginRequest(testEmail, testPassword);
        when(mockUserService.getUserOrThrowByEmail(testEmail))
                .thenReturn(getTestUser(testEmail, "12345", testAuthority));

        Throwable throwable = assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));

        assertThat(throwable.getMessage(), equalTo(ErrorMsg.PASSWORD_NOT_MATCH));
    }

    @Test
    @DisplayName("유효한 로그인 요청이 들어오면 JwtTokenDto 를 반환한다.")
    public void test_03() {

        LoginRequest loginRequest = new LoginRequest(testEmail, testPassword);
        User user = getTestUser(testEmail, passwordEncoder.encode(testPassword), testAuthority);
        when(mockUserService.getUserOrThrowByEmail(testEmail)).thenReturn(user);
        when(mockJwtProvider.createAccessToken(testEmail, testAuthority)).thenReturn("accessToken");
        when(mockJwtProvider.createRefreshToken(testEmail, testAuthority)).thenReturn("refreshToken");

        JwtTokenDto jwtTokenDto = authService.login(loginRequest);

        assertThat(jwtTokenDto.getGrantType(), equalTo(AuthConstants.GRANT_TYPE_BEARER));
        assertThat(jwtTokenDto.getAccessToken(), equalTo("accessToken"));
        assertThat(jwtTokenDto.getRefreshToken(), equalTo("refreshToken"));
    }

    @Test
    @DisplayName("토큰 재발급시 인증타입이 유효하지 않으면 JwtInvalidException 을 던진다.")
    public void test_04() {

        Throwable throwable = assertThrows(JwtInvalidException.class, () -> authService.reissue("refreshToken"));
        assertThat(throwable.getMessage(), equalTo(ErrorMsg.INVALID_GRANT_TYPE));
    }

    @Test
    @DisplayName("토큰 재발급시 claim 이 null 이면 JwtInvalidException 을 던진다.")
    public void test_05() {

        when(mockJwtProvider.parseClaimsFromRefreshToken("refreshToken")).thenReturn(null);

        Throwable throwable = assertThrows(JwtInvalidException.class, () -> authService.reissue("Bearer refreshToken"));
        assertThat(throwable.getMessage(), equalTo(ErrorMsg.CLAIM_NOT_EXIST));
    }

    @Test
    @DisplayName("토큰 재발급시 유효한 refreshToken 이 주어지면 JwtTokenDto 를 반환한다.")
    public void test_06() {

        User user = getTestUser(testEmail, testPassword, testAuthority);
        Claims claims = Jwts.claims().setSubject(testEmail);
        claims.put(AuthConstants.KEY_ROLES, Collections.singleton(testAuthority));

        when(mockUserService.getUserOrThrowByEmail(testEmail)).thenReturn(user);
        when(mockJwtProvider.parseClaimsFromRefreshToken("refreshToken")).thenReturn(claims);
        when(mockJwtProvider.createAccessToken(testEmail, testAuthority)).thenReturn("accessToken");
        when(mockJwtProvider.createRefreshToken(testEmail, testAuthority)).thenReturn("refreshToken");

        JwtTokenDto jwtTokenDto = authService.reissue("Bearer refreshToken");

        assertThat(jwtTokenDto.getGrantType(), equalTo(AuthConstants.GRANT_TYPE_BEARER));
        assertThat(jwtTokenDto.getAccessToken(), equalTo("accessToken"));
        assertThat(jwtTokenDto.getRefreshToken(), equalTo("refreshToken"));
    }

    @Test
    @DisplayName("동일한 이메일로 회원가입을 시도하면 에러를 던진다.")
    public void test_07() {
        RegisterRequest request = RegisterRequest.builder()
                .email(testEmail)
                .password(passwordEncoder.encode(testPassword))
                .build();

        doThrow(new DuplicatedArgumentException(testEmail + ErrorMsg.DUPLICATED))
                .when(mockUserService).throwIfUserExistByEmail(testEmail);

        Throwable throwable = assertThrows(RuntimeException.class, () -> authService.register(request));

        assertThat(throwable.getMessage(), equalTo(testEmail + " already exist"));
    }

    @Test
    @DisplayName("회원가입에 성공하면 UserDto 를 반환해야 하며 초기 사용자 레벨은 고마운분이다.")
    public void test_09() {
        // given
        RegisterRequest request = RegisterRequest.builder()
                .email(testEmail)
                .password(passwordEncoder.encode(testPassword))
                .build();
        User user = User.builder()
                .id(1L)
                .email(testEmail)
                .address(new Address("seoul", "songpa", "12345"))
                .level(Level.builder().name("고마운분").build())
                .build();

        // when
        when(mockUserService.save(any())).thenReturn(user);
        UserInfoDto dto = authService.register(request);

        // then
        verify(mockUserService, times(1)).throwIfUserExistByEmail(testEmail);

        assertThat(dto.getId(), equalTo(1L));
        assertThat(dto.getLevel(), equalTo("고마운분"));
        assertThat(dto.getAddress(), equalTo("seoul songpa 12345"));
        assertThat(dto.getEmail(), equalTo(testEmail));
    }
}