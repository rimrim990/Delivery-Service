package com.project.deliveryservice.domain.auth.service;

import com.project.deliveryservice.common.constants.AuthConstants;
import com.project.deliveryservice.domain.auth.dto.LoginRequest;
import com.project.deliveryservice.domain.user.entity.User;
import com.project.deliveryservice.domain.user.repository.UserRepository;
import com.project.deliveryservice.jwt.JwtTokenDto;
import com.project.deliveryservice.jwt.JwtInvalidException;
import com.project.deliveryservice.jwt.JwtTokenProvider;
import com.project.deliveryservice.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public JwtTokenDto login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(request.getEmail() + " is not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("password is not match");
        }

        return createJwtDto(user);
    }

    public JwtTokenDto reissue(String bearerToken) {
        String refreshToken = JwtUtils.resolveJwtToken(bearerToken);
        if (!StringUtils.hasText(refreshToken)) {
            throw new JwtInvalidException("invalid grant type");
        }

        Claims claims = jwtTokenProvider.parseClaimsFromJwtToken(refreshToken);
        if (claims == null) {
            throw new JwtInvalidException("claim not exist in token");
        }

        User user = userRepository.findByEmail(claims.getSubject())
                .orElseThrow(() -> new UsernameNotFoundException(claims.getSubject() + " is not found"));

        return createJwtDto(user);
    }


    private JwtTokenDto createJwtDto(User user) {
        String email = user.getEmail();
        String authority = user.getLevel().getAuthority();
        return JwtTokenDto.builder()
                .grantType(AuthConstants.GRANT_TYPE_BEARER)
                .accessToken(jwtTokenProvider.createAccessToken(email, authority))
                .refreshToken(jwtTokenProvider.createRefreshToken(email, authority))
                .build();
    }
}