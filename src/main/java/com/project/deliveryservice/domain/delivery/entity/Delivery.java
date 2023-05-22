package com.project.deliveryservice.domain.delivery.entity;

import com.project.deliveryservice.common.entity.Address;
import com.project.deliveryservice.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

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
}
