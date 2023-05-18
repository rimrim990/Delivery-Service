package com.project.deliveryservice.jwt;

import com.project.deliveryservice.common.constants.AuthConstants;
import io.jsonwebtoken.Claims;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
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
    public void givenNotSupportAuthentication_whenCallSupports_thenReturnFalse() {

        assertThat(provider.supports(UsernamePasswordAuthenticationToken.class), equalTo(false));
        assertThat(provider.supports(AbstractAuthenticationToken.class), equalTo(false));
        assertThat(provider.supports(Authentication.class), equalTo(false));
    }

    @Test
    public void givenSupportAuthentication_whenCallSupports_thenReturnTrue() {

        assertThat(provider.supports(JwtAuthenticationToken.class), equalTo(true));
    }

    @Test
    public void givenTokenMadeByDifferentSecretKey_whenCallAuthentication_thenThrowJwtInvalidException() {

        String invalidSecretKey = "invalidSecretKeyInvalidInvalidInvalid";
        String invalidToken = createToken("test", Collections.singletonList("ADMIN"), new Date(), 30, invalidSecretKey);
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(invalidToken);

        Throwable throwable = assertThrows(JwtInvalidException.class, () -> provider.authenticate(authentication));

        assertThat(throwable, isA(JwtInvalidException.class));
        assertThat(throwable.getMessage(), equalTo("signature key is different"));
    }

    @Test
    public void givenExpiredToken_whenCallAuthentication_thenThrowJwtInvalidException() {

        Date past = new Date(System.currentTimeMillis() - ONE_MINUTE * 10);
        String invalidToken = createToken("test", Collections.singletonList("ADMIN"), past, 5, SECRET);
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(invalidToken);

        Throwable throwable = assertThrows(JwtInvalidException.class, () -> provider.authenticate(authentication));

        assertThat(throwable, isA(JwtInvalidException.class));
        assertThat(throwable.getMessage(), equalTo("expired token"));
    }

    @Test
    public void givenMalformedToken_whenCallAuthentication_thenThrowJwtInvalidException() {

        JwtAuthenticationToken authentication = new JwtAuthenticationToken("some malformed token here");

        Throwable throwable = assertThrows(JwtInvalidException.class, () -> provider.authenticate(authentication));

        assertThat(throwable, isA(JwtInvalidException.class));
        assertThat(throwable.getMessage(), equalTo("malformed token"));
    }

    @Test
    public void givenNullJwt_whenCallAuthentication_thenThrowJwtInvalidException() {

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(null);

        Throwable throwable = assertThrows(JwtInvalidException.class, () -> provider.authenticate(authentication));

        assertThat(throwable, isA(JwtInvalidException.class));
        assertThat(throwable.getMessage(), equalTo("using illegal argument like null"));
    }

    @Test
    public void givenValidToken_whenCallAuthentication_thenReturnAuthentication() {

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