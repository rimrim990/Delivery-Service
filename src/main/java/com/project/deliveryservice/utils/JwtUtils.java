package com.project.deliveryservice.utils;

import com.project.deliveryservice.common.constants.AuthConstants;
import com.project.deliveryservice.common.exception.ErrorMsg;
import com.project.deliveryservice.jwt.JwtInvalidException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;

import java.security.Key;

public class JwtUtils {

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
}
