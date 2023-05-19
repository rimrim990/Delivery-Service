package com.project.deliveryservice.jwt;

import com.project.deliveryservice.utils.JwtUtils;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtTokenProvider {
    
    private final Key key;
    private final Key refreshKey;
    private final int expireMin;
    private final int refreshExpireMin;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.refresh-secret}") String refreshSecretKey,
            @Value("${jwt.expire-min}") int expireMin,
            @Value("${jwt.refresh-expire-min}") int refreshExpireMin) {

        this.key = JwtUtils.generateKey(secretKey);
        this.refreshKey = JwtUtils.generateKey(refreshSecretKey);

        this.expireMin = expireMin;
        this.refreshExpireMin = refreshExpireMin;
    }

    public String createAccessToken(String email, String authority) {
        return JwtUtils.createJwtToken(email, authority, expireMin, key);
    }

    public String createRefreshToken(String email, String authority) {
        return JwtUtils.createJwtToken(email, authority, refreshExpireMin, refreshKey);
    }

    public Claims parseClaimsFromRefreshToken(String jwt) {
        return JwtUtils.parseClaimsFromJwt(refreshKey, jwt);
    }
}
