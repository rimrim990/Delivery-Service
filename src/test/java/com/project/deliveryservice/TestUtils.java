package com.project.deliveryservice;

import com.project.deliveryservice.common.entity.Address;
import com.project.deliveryservice.domain.user.entity.Level;
import com.project.deliveryservice.domain.user.entity.Role;
import com.project.deliveryservice.domain.user.entity.User;
import com.project.deliveryservice.utils.JwtUtils;

import java.security.Key;

public class TestUtils {

    public static final String testEmail = "test@naver.com";

    public static final String testPassword = "1234567890";

    public static final String testAuthority = "ROLE_NORMAL";

    public static final int testExpireMin = 10;

    public static final int testRefreshExpireMin = 30;

    /**
     * 토큰 생성
     * */
    public static String getTestAccessToken(Key secretKey) {
        return JwtUtils.createJwtToken(testEmail, testAuthority, testExpireMin, secretKey);
    }

    public static String getTestRefreshToken(Key refreshSecretKey) {
        return JwtUtils.createJwtToken(testEmail, testAuthority, testRefreshExpireMin, refreshSecretKey);
    }

    /**
     * 사용자 정보 조회
     * */
    public static User getTestUser(String email, String encodedPassword, String authority) {
        Level level = Level.builder()
                .role(Role.valueOf(authority))
                .build();

        return User.builder()
                .email(email)
                .password(encodedPassword)
                .level(level)
                .build();
    }

    public static User getDefaultTestUser(Long id, String email, Address address) {
        Level level = Level.builder()
                .name("고마운분")
                .build();

        return User.builder()
                .id(id)
                .email(email)
                .address(address)
                .level(level)
                .build();
    }

}
