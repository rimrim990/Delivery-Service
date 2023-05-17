package com.project.deliveryservice.jwt;

import com.project.deliveryservice.common.constants.AuthConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final byte[] secretKeyBytes;

    public JwtAuthenticationProvider(@Value("${jwt.secret}") String secretKey) {
        this.secretKeyBytes = secretKey.getBytes();
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(secretKeyBytes)
                    .build()
                    .parseClaimsJws(((JwtAuthenticationToken) authentication).getJsonWebToken())
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
        return new JwtAuthenticationToken(claims.getSubject(), "", createGrantedAuthorities(claims));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private Collection<? extends GrantedAuthority> createGrantedAuthorities(Claims claims) {
        List<String> roles = (List) claims.get(AuthConstants.KEY_ROLES);
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (String role : roles) {
            grantedAuthorities.add(() -> role);
        }
        return grantedAuthorities;
    }
}
