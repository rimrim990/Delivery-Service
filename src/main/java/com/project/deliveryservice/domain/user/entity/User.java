package com.project.deliveryservice.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.deliveryservice.common.entity.Address;
import com.project.deliveryservice.common.entity.ExtendedTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends ExtendedTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Size(min = 3, max= 12)
    private String username;

    @ManyToOne
    @JoinColumn(name = "level_id")
    private Level level;

    @Embedded
    private Address address;

    @JsonIgnore
    public boolean isEnabled() {
        return getDeletedAt() == null;
    }
}
