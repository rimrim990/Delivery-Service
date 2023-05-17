package com.project.deliveryservice.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Level implements GrantedAuthority {

    @Id @GeneratedValue
    private Long id;

    private String name;

    private int policy;

    @Enumerated(value = EnumType.STRING)
    private Grade grade;

    @Override
    public String getAuthority() {
        return grade.toString();
    }
}
