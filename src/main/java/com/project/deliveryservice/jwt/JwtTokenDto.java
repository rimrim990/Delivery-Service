package com.project.deliveryservice.jwt;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JwtTokenDto {

    private String grantType;

    private String accessToken;

    private String refreshToken;
}
