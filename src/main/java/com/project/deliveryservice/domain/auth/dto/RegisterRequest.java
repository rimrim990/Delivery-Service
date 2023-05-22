package com.project.deliveryservice.domain.auth.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    private String password;

    @NotEmpty
    @Length(min=3, max=12)
    private String username;

    @NotEmpty
    private String city;

    @NotEmpty
    private String street;

    @NotEmpty
    @Digits(integer = 5, fraction = 0)
    private String zipCode;
}
