package com.project.deliveryservice.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RegisterRequest {

    private String email;

    private String password;

    private String username;

    private String city;

    private String street;

    private String zipCode;
}
