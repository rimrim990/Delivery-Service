package com.project.deliveryservice.common.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class ExtendedTimeEntity extends BaseTimeEntity {

    private LocalDateTime deletedAt;
}
