package com.project.deliveryservice.jwt;

import com.project.deliveryservice.common.constants.AuthConstants;
import com.project.deliveryservice.utils.JwtUtils;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final long ONE_SECONDS = 1000;
    private static final long ONE_MINUTE = 60 * ONE_SECONDS;

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

    public String createToken(String email, String authority, Key key, int expireMin) {
        Date now = new Date();
        Claims claims = Jwts.claims().setSubject(email);
        claims.put(AuthConstants.KEY_ROLES, Collections.singleton(authority));
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ONE_MINUTE * expireMin))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String createAccessToken(String email, String authority) {
        return createToken(email, authority, key, expireMin);
    }

    public String createRefreshToken(String email, String authority) {
        return createToken(email, authority, refreshKey, refreshExpireMin);
    }

    public Claims parseClaimsFromRefreshToken(String jwt) {
        return JwtUtils.parseClaimsFromJwt(refreshKey, jwt);
    }
}
