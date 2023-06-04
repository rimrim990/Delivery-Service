package com.project.deliveryservice.domain.auth.service;

import com.project.deliveryservice.common.constants.AuthConstants;
import com.project.deliveryservice.common.entity.Address;
import com.project.deliveryservice.common.exception.ErrorMsg;
import com.project.deliveryservice.domain.auth.dto.LoginRequest;
import com.project.deliveryservice.domain.auth.dto.RegisterRequest;
import com.project.deliveryservice.domain.user.dto.UserInfo;
import com.project.deliveryservice.domain.user.entity.Level;
import com.project.deliveryservice.domain.user.entity.Role;
import com.project.deliveryservice.domain.user.entity.User;
import com.project.deliveryservice.domain.user.service.LevelService;
import com.project.deliveryservice.domain.user.service.UserService;
import com.project.deliveryservice.jwt.JwtTokenDto;
import com.project.deliveryservice.jwt.JwtInvalidException;
import com.project.deliveryservice.jwt.JwtTokenProvider;
import com.project.deliveryservice.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final LevelService levelService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public JwtTokenDto login(LoginRequest request) {
        User user = userService.getUserOrThrowByEmail(request.getEmail());

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

        User user = userService.getUserOrThrowByEmail(claims.getSubject());

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

    public UserInfo register(RegisterRequest request) {
        // 동일한 이메일로 이미 회원가입 되어있음
        userService.throwIfUserExistByEmail(request.getEmail());
        // 기본 레벨 조회
        Level defaultLevel = levelService.getLevelOrThrowByRole(Role.ROLE_NORMAL);

        Address address = new Address(request.getCity(), request.getStreet(), request.getZipCode());
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .level(defaultLevel)
                .password(passwordEncoder.encode(request.getPassword()))
                .address(address).build();

        return UserInfo.of(userService.save(user));
    }
}
