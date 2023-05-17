package com.project.deliveryservice.domain.user.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;

@Entity
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
