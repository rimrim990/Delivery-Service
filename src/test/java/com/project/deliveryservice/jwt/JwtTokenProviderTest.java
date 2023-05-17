package com.project.deliveryservice.jwt;

import com.project.deliveryservice.common.constants.AuthConstants;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtTokenProviderTest {

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("accessToken 값을 기반으로 생성된 claim 은 토큰과 동일한 값을 갖는다.")
    public void givenUser_whenCreateAccessTokenByUser_thenParsedClaimsWithSameValue() {

        String jwt = jwtTokenProvider.createAccessToken("test", "Admin");

        Claims claims = jwtTokenProvider.parseClaimsFromJwtToken(jwt);

        assertThat(claims.getSubject(), equalTo("test"));
        assertThat(claims.get(AuthConstants.KEY_ROLES), isA(List.class));
        List<String> roles = (List) claims.get(AuthConstants.KEY_ROLES);
        for (String role : roles) {
            assertThat(role, equalTo("Admin"));
        }
    }

    @Test
    @DisplayName("refreshToken 값을 기반으로 생성된 claim 은 토큰과 동일한 값을 갖는다.")
    public void givenUser_whenCreateRefreshTokenByUser_thenParsedClaimsWIthSameValue() {

        String jwt = jwtTokenProvider.createRefreshToken("test", "Admin");

        Claims claims = jwtTokenProvider.parseClaimsFromJwtToken(jwt);

        assertThat(claims.getSubject(), equalTo("test"));
        assertThat(claims.get(AuthConstants.KEY_ROLES), isA(List.class));
        List<String> roles = (List) claims.get(AuthConstants.KEY_ROLES);
        for (String role : roles) {
            assertThat(role, equalTo("Admin"));
        }
    }

    @Test
    @DisplayName("유효하지 않은 refreshToken 이 주어지면 claim 파싱 과정에서 JwtInvalidException 을 던진다.")
    public void givenInvalidRefreshToken_whenParseClaimsFromJwtToken_thenThrowJwtInvalidException() {

        String invalidRefreshToken = "invalid refresh token";

        Throwable throwable = assertThrows(JwtInvalidException.class, () -> jwtTokenProvider.parseClaimsFromJwtToken(invalidRefreshToken));

        assertThat(throwable.getMessage(), equalTo("malformed token"));
    }
}