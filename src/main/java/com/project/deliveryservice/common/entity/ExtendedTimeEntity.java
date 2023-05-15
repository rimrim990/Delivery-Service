package com.project.deliveryservice.common.entity;

import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class ExtendedTimeEntity extends BaseTimeEntity {

    private LocalDateTime deletedAt;
}
