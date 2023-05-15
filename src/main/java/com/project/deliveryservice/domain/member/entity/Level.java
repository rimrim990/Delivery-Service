package com.project.deliveryservice.domain.member.entity;

import jakarta.persistence.*;

@Entity
public class Level {

    @Id @GeneratedValue
    private Long id;

    private String name;

    private int policy;

    @Enumerated(value = EnumType.STRING)
    private Grade grade;
}
