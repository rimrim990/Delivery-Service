package com.project.deliveryservice.domain.shop.entity;

import com.project.deliveryservice.common.entity.Address;
import com.project.deliveryservice.common.entity.ExtendedTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access =  AccessLevel.PROTECTED)
@Entity
public class Shop extends ExtendedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Enumerated(value = EnumType.STRING)
    public Category category;

    @Column()
    // 최소 주문 금액
    public int minPrice;

    @Embedded
    public Address address;

    @Column()
    // 가게 설명
    public String description;

    @Column()
    // 휴무일
    public int restDays;

    @Column()
    // 영업 시작 시간
    public LocalTime startTime;

    @Column()
    // 영업 종료 시간
    public LocalTime endTime;
}
