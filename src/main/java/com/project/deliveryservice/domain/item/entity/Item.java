package com.project.deliveryservice.domain.item.entity;

import com.project.deliveryservice.common.entity.BaseTimeEntity;
import com.project.deliveryservice.domain.shop.entity.Shop;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Item extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Size(min=1, max=20)
    @Column(nullable = false)
    private String name;

    @Size(max=100)
    @Column(nullable = true)
    private String description;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    // 사장님 추천 여가
    private boolean isRecommended;
}
