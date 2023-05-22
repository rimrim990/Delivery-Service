package com.project.deliveryservice.domain.item.entity;

import com.project.deliveryservice.common.entity.BaseTimeEntity;
import com.project.deliveryservice.domain.shop.entity.Shop;
import jakarta.persistence.*;
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

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    private String name;

    private String description;

    private int price;

    @Column(columnDefinition = "사장님 추천 여부")
    private boolean isRecommended;
}
