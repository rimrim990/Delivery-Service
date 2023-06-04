package com.project.deliveryservice.domain.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ShopInfo {

    private Long shopId;

    private String shopName;

    private String address;

    private String description;

    private String category;

    // 휴무일

    // 영업시간
}
