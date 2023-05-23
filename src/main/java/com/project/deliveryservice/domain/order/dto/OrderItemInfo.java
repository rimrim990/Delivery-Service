package com.project.deliveryservice.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderItemInfo {

    // 주문한 아이템의 아이디
    private Long itemId;

    // 주문한 아이템 이름
    private String itemName;

    // 주문한 아이템 수량
    private int quantity;

    // 주문한 아이템 가격
    private int price;
}
