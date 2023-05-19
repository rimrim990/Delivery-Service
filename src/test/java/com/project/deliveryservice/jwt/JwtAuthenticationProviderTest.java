package com.project.deliveryservice.jwt;

import com.project.deliveryservice.common.exception.ErrorMsg;

import com.project.deliveryservice.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.security.Key;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtAuthenticationProviderTest {

    private final int expireMin = 10;
    @Value("${jwt.secret}")
    private String secret;
    private Key secretKey;

    private final String test_email = "test";
    private final String test_authority = "ROLE_ADMIN";

    JwtAuthenticationProvider provider;

    @BeforeEach
    public void setup() {
        provider = new JwtAuthenticationProvider(secret);
        secretKey = JwtUtils.generateKey(secret);
    }

    @Test
    @DisplayName("지원하지 않는 Authentication 구현체를 인자로 support 를 호춣하면 false 를 반환한다.")
    public void test_01() {

        assertThat(provider.supports(UsernamePasswordAuthenticationToken.class), equalTo(false));
        assertThat(provider.supports(AbstractAuthenticationToken.class), equalTo(false));
        assertThat(provider.supports(Authentication.class), equalTo(false));
    }

    @Test
    @DisplayName("지원하는 Authentication 구현체를 인자로 support 를 호출하면 true 를 반환한다.")
    public void test_02() {

        assertThat(provider.supports(JwtAuthenticationToken.class), equalTo(true));
    }

    @Test
    @DisplayName("다른 비밀키로 만든 토큰을 인자로 authentication 을 호출하면 JwtInvalidException 을 던진다.")
    public void test_03() {

        String invalidSecret = "invalidSecretKeyInvalidInvalidInvalidinvalidSecretKeyInvalidInvalidInvalidinvalidSecret";
        Key invalidSecretKey = JwtUtils.generateKey(invalidSecret);
        String invalidToken = JwtUtils.createJwtToken(test_email, test_authority, expireMin, invalidSecretKey);
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(invalidToken);

        Throwable throwable = assertThrows(JwtInvalidException.class, () -> provider.authenticate(authentication));

        assertThat(throwable, isA(JwtInvalidException.class));
        assertThat(throwable.getMessage(), equalTo(ErrorMsg.DIFFERENT_SIGNATURE_KEY));
    }

    @Test
    @DisplayName("만료된 토큰을 인자로 authentication 을 호출하면 JwtInvalidException 을 던진다.")
    public void test_04() {

        String invalidToken = JwtUtils.createJwtToken(test_email, test_authority, -expireMin, secretKey);
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(invalidToken);

        Throwable throwable = assertThrows(JwtInvalidException.class, () -> provider.authenticate(authentication));

        assertThat(throwable, isA(JwtInvalidException.class));
        assertThat(throwable.getMessage(), equalTo(ErrorMsg.TOKEN_EXPIRED));
    }

    @Test
    @DisplayName("잘못된 형식의 토큰을 인자로 authentication 을 호출하면 JwtInvalidException 을 던진다.")
    public void test_05() {

        JwtAuthenticationToken authentication = new JwtAuthenticationToken("some malformed token here");

        Throwable throwable = assertThrows(JwtInvalidException.class, () -> provider.authenticate(authentication));

        assertThat(throwable, isA(JwtInvalidException.class));
        assertThat(throwable.getMessage(), equalTo("malformed token"));
    }

    @Test
    @DisplayName("null 토큰 값을 인자로 authentication 을 호출하면 JwtInvalidException 을 던진다.")
    public void test_06() {

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(null);

        Throwable throwable = assertThrows(JwtInvalidException.class, () -> provider.authenticate(authentication));

        assertThat(throwable, isA(JwtInvalidException.class));
        assertThat(throwable.getMessage(), equalTo(ErrorMsg.ILLEGAL_TOKEN));
    }

    @Test
    @DisplayName("유효한 토큰을 인자로 authentication 을 호출하면 authentication 을 반환한다.")
    public void test_07() {

        String validToken = JwtUtils.createJwtToken(test_email, test_authority, expireMin, secretKey);
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(validToken);

        Authentication authenticated = provider.authenticate(authentication);

        assertThat(authenticated.getPrincipal(), equalTo(test_email));
        assertThat(authenticated.getCredentials(), equalTo(""));
        Collection<? extends GrantedAuthority> authorities = authenticated.getAuthorities();
        for (GrantedAuthority grantedAuthority : authorities) {
            assertThat(grantedAuthority.getAuthority(), equalTo(test_authority));
        }
    }
}