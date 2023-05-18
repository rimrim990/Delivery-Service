package com.project.deliveryservice.jwt;

import com.project.deliveryservice.common.constants.AuthConstants;
import io.jsonwebtoken.Claims;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationProviderTest {

    final int ONE_SECONDS = 1000;
    final int ONE_MINUTE = 60 * ONE_SECONDS;
    final String SECRET = "aGVlbG9hc2RqZmtsc2FqO2xrdmphbGtkZmo7ZGtmamFsZGZqa2FkYWxqa2xhaztqZmthc2RmYXNkZmFzZGZhc2RmYXNkZmFzZGZhZHNzZGZhc2ZkYXNmZGFzZGZhc2Rmc2RmYQ==";

    JwtAuthenticationProvider provider;

    @BeforeEach
    public void setup() {
        provider = new JwtAuthenticationProvider(SECRET);
    }

    private String createToken(String username, List<String> roles, Date now, int expireMin, String secretKey) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put(AuthConstants.KEY_ROLES, roles);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ONE_MINUTE * expireMin))
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();
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

        String invalidSecretKey = "invalidSecretKeyInvalidInvalidInvalid";
        String invalidToken = createToken("test", Collections.singletonList("ADMIN"), new Date(), 30, invalidSecretKey);
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(invalidToken);

        Throwable throwable = assertThrows(JwtInvalidException.class, () -> provider.authenticate(authentication));

        assertThat(throwable, isA(JwtInvalidException.class));
        assertThat(throwable.getMessage(), equalTo("signature key is different"));
    }

    @Test
    @DisplayName("만료된 토큰을 인자로 authentication 을 호출하면 JwtInvalidException 을 던진다.")
    public void test_04() {

        Date past = new Date(System.currentTimeMillis() - ONE_MINUTE * 10);
        String invalidToken = createToken("test", Collections.singletonList("ADMIN"), past, 5, SECRET);
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(invalidToken);

        Throwable throwable = assertThrows(JwtInvalidException.class, () -> provider.authenticate(authentication));

        assertThat(throwable, isA(JwtInvalidException.class));
        assertThat(throwable.getMessage(), equalTo("expired token"));
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
        assertThat(throwable.getMessage(), equalTo("using illegal argument like null"));
    }

    @Test
    @DisplayName("유효한 토큰을 인자로 authentication 을 호출하면 authentication 을 반환한다.")
    public void test_07() {

        String validToken = createToken("test", Collections.singletonList("ADMIN"), new Date(), 30, SECRET);
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(validToken);

        Authentication authenticated = provider.authenticate(authentication);

        assertThat(authenticated.getPrincipal(), equalTo("test"));
        assertThat(authenticated.getCredentials(), equalTo(""));
        Collection<? extends GrantedAuthority> authorities = authenticated.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            assertThat(authority.getAuthority(), equalTo("ADMIN"));
        }
    }
}