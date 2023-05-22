package com.project.deliveryservice.domain.auth.service;

import com.project.deliveryservice.common.constants.AuthConstants;
import com.project.deliveryservice.common.entity.Address;
import com.project.deliveryservice.common.exception.DuplicatedArgumentException;
import com.project.deliveryservice.common.exception.ErrorMsg;
import com.project.deliveryservice.domain.auth.dto.LoginRequest;
import com.project.deliveryservice.domain.auth.dto.RegisterRequest;
import com.project.deliveryservice.domain.user.dto.UserInfoDto;
import com.project.deliveryservice.domain.user.entity.Level;
import com.project.deliveryservice.domain.user.entity.Role;
import com.project.deliveryservice.domain.user.entity.User;
import com.project.deliveryservice.domain.user.repository.LevelRepository;
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
    private final LevelRepository levelRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public JwtTokenDto login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(request.getEmail() + " is not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException(ErrorMsg.PASSWORD_NOT_MATCH);
        }

        return createJwtDto(user);
    }

    public JwtTokenDto reissue(String bearerToken) {
        String refreshToken = JwtUtils.resolveJwtToken(bearerToken);
        if (!StringUtils.hasText(refreshToken)) {
            throw new JwtInvalidException(ErrorMsg.INVALID_GRANT_TYPE);
        }

        Claims claims = jwtTokenProvider.parseClaimsFromRefreshToken(refreshToken);
        if (claims == null) {
            throw new JwtInvalidException(ErrorMsg.CLAIM_NOT_EXIST);
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

    public UserInfoDto register(RegisterRequest request) {
        // 동일한 이메일로 이미 회원가입 되어있음
        userRepository.findByEmail(request.getEmail())
                .ifPresent(u -> {
                    throw new DuplicatedArgumentException(u.getEmail() + ErrorMsg.DUPLICATED);
                } );

        Level defaultLevel = levelRepository.findByRole(Role.ROLE_NORMAL)
                .orElseThrow(() -> new RuntimeException("internal server error"));

        Address address = new Address(request.getCity(), request.getStreet(), request.getZipCode());
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .level(defaultLevel)
                .password(passwordEncoder.encode(request.getPassword()))
                .address(address).build();

        return UserInfoDto.of(userRepository.save(user));
    }
}
