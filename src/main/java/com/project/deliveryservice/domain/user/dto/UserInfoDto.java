package com.project.deliveryservice.domain.user.dto;

import com.project.deliveryservice.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserInfoDto {

    private long id;

    private String email;

    private String address;

    private String level;

    public static UserInfoDto of(User source) {
        return UserInfoDto.builder()
                .id(source.getId())
                .email(source.getEmail())
                .address(source.getAddress().toString())
                .level(source.getLevel().getName())
                .build();
    }
}
