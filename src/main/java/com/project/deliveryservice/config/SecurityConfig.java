package com.project.deliveryservice.config;

import com.project.deliveryservice.jwt.JwtAuthenticationProvider;
import com.project.deliveryservice.jwt.JwtSecurityConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final String ROLE_ADMIN = "ADMIN";
    private final String ROLE_NORMAL = "NORMAL";
    private final String ROLE_VIP = "VIP";

    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                // 요청 별 인증 필요 여부 혹은 권한 확인
                .authorizeHttpRequests()
                // api/auth 로 시작하는 모든 경로는 권한 없이 수행 가능
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest()
                .hasAnyRole(ROLE_NORMAL, ROLE_VIP, ROLE_ADMIN)
                // h2-console 사용을 위한 설정
                .and()
                .headers()
                .frameOptions()
                .sameOrigin()
                // 세션을 사용하지 않도록 변경
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                // JWT 토큰 인증 필터 설정
                .and()
                .apply(new JwtSecurityConfig(authenticationManager()));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        this.authenticationManagerBuilder.authenticationProvider(jwtAuthenticationProvider);
        return authenticationManagerBuilder.getOrBuild();
    }
}
