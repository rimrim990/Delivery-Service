package com.project.deliveryservice.domain.user.entity;

import com.project.deliveryservice.common.entity.Address;
import com.project.deliveryservice.common.entity.ExtendedTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class User extends ExtendedTimeEntity {
    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToOne
    @JoinColumn(name = "level_id")
    private Level level;

    @Embedded
    private Address address;
}
