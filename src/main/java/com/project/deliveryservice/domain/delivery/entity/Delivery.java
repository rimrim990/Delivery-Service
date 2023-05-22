package com.project.deliveryservice.domain.delivery.entity;

import com.project.deliveryservice.common.entity.Address;
import com.project.deliveryservice.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Delivery extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @Column(nullable = false)
    private Address address;

    // 배달 기사 배정 시각
    private LocalDateTime allocatedAt;

    // 배달 수령 시각
    private LocalDateTime pickupedAt;

    // 배달 완료 시각
    private LocalDateTime completedAt;

    // 배차 취소 시각
    private LocalDateTime canceledAt;
}
