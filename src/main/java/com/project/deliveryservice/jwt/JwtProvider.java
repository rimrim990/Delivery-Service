package com.project.deliveryservice.jwt;

import com.project.deliveryservice.common.constants.AuthConstants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Component
public class JwtProvider {

    private static final long ONE_SECONDS = 1000;
    private static final long ONE_MINUTE = 60 * ONE_SECONDS;

    private final Key key;
    private final int expireMin;
    private final int refreshExpireMin;

    public JwtProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expire-min}") int expireMin,
            @Value("${jwt.refresh-expire-min}") int refreshExpireMin) {

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);

        this.expireMin = expireMin;
        this.refreshExpireMin = refreshExpireMin;
    }

    public String createToken(String email, String authority, int expireMin) {
        Date now = new Date();
        Claims claims = Jwts.claims().setSubject(email);
        claims.put(AuthConstants.KEY_ROLES, Collections.singleton(authority));
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ONE_MINUTE * expireMin))
                .signWith(this.key, SignatureAlgorithm.ES512)
                .compact();
    }

    public String createAccessToken(String email, String authority) {
        return createToken(email, authority, expireMin);
    }

    public String createRefreshToken(String email, String authority) {
        return createToken(email, authority, refreshExpireMin);
    }

    public Claims parseClaimsFromRefreshToken(String jwt) {
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
        } catch (SignatureException signatureException) {
            throw new JwtInvalidException("signature key is different", signatureException);
        } catch (ExpiredJwtException expiredJwtException) {
            throw new JwtInvalidException("expired token", expiredJwtException);
        } catch (MalformedJwtException malformedJwtException) {
            throw new JwtInvalidException("malformed token", malformedJwtException);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new JwtInvalidException("using illegal argument like null", illegalArgumentException);
        }
        return claims;
    }
}
