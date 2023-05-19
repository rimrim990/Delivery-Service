package com.project.deliveryservice.utils;

import com.project.deliveryservice.common.constants.AuthConstants;
import com.project.deliveryservice.common.exception.ErrorMsg;
import com.project.deliveryservice.jwt.JwtInvalidException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

public class JwtUtils {

    private static final long ONE_SECONDS = 1000;
    private static final long ONE_MINUTE = 60 * ONE_SECONDS;

    /**
     *
     * @param request HttpServletRequest
     * @return HTTP 요청 헤더에 포함된 Bearer 토큰 값
     */
    public static String resolveJwtToken(HttpServletRequest request) {
        String bearerToken = getTokenFromHeader(request);
        return JwtUtils.resolveJwtToken(bearerToken);
    }

    /**
     *
     * @param bearerToken Grant 타입이 Bearer 인 토큰
     * @return bearerToken 에서 prefix 를 제거한 실제 토큰
     */
    public static String resolveJwtToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AuthConstants.BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private static String getTokenFromHeader(HttpServletRequest request) {
        return request.getHeader(AuthConstants.AUTHORIZATION_HEADER);
    }

    /**
     *
     * @param secretKey Key 인스턴스 생성에 사용될 Base64로 인코딩된 비밀키
     * @return secretKey 로부터 생성된 Key 인스턴스
     */
    public static Key generateKey(String secretKey) {
        byte[] secretKeyByte = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(secretKeyByte);
    }

    /**
     *
     * @param secretKey jwt 토큰 디코딩을 위한 비밀키
     * @param jwt Claim 을 추출할 jwt 토큰
     * @return jwt 에서 추출된 claim
     * @throws AuthenticationException
     */
    public static Claims parseClaimsFromJwt(Key secretKey, String jwt) throws AuthenticationException {
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
        } catch (SignatureException signatureException) {
            throw new JwtInvalidException(ErrorMsg.DIFFERENT_SIGNATURE_KEY, signatureException);
        } catch (ExpiredJwtException expiredJwtException) {
            throw new JwtInvalidException(ErrorMsg.TOKEN_EXPIRED, expiredJwtException);
        } catch (MalformedJwtException malformedJwtException) {
            throw new JwtInvalidException(ErrorMsg.TOKEN_MALFORMED, malformedJwtException);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new JwtInvalidException(ErrorMsg.ILLEGAL_TOKEN, illegalArgumentException);
        }
        return claims;
    }

    /**
     *
     * @param email jwt 토큰 claim 에 포함될 사용자 이메일
     * @param authority jwt 토큰 claim 에 포함될 사용자 권한
     * @param expireMin jwt 토큰 만료 시간 (분 단위)
     * @param key jwt 토큰 암호화에 사용될 Key 인스턴스
     * @return jwt 토큰
     */
    public static String createJwtToken(String email, String authority, int expireMin, Key key) {
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
}
