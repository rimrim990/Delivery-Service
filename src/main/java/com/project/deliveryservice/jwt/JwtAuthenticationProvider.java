package com.project.deliveryservice.jwt;

import com.project.deliveryservice.common.constants.AuthConstants;
import com.project.deliveryservice.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final Key secretKey;

    public JwtAuthenticationProvider(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = JwtUtils.generateKey(secretKey);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String jwt = ((JwtAuthenticationToken) authentication).getJsonWebToken();
        Claims claims = JwtUtils.parseClaimsFromJwt(secretKey, jwt);
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
