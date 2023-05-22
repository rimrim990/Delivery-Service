package com.project.deliveryservice.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Level implements GrantedAuthority {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int policy;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    @Override
    public String getAuthority() {
        return role.toString();
    }
}
