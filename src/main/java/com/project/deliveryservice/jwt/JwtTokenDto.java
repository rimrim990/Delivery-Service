package com.project.deliveryservice.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenDto {

    private String grantType;

    private String accessToken;

    private String refreshToken;
}
