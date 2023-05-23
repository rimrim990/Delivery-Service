package com.project.deliveryservice.domain.order.dto;

import com.project.deliveryservice.domain.order.entity.OrderItem;
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

    public static OrderItemInfo of(OrderItem source) {
        return OrderItemInfo.builder()
                .itemId(source.getItem().getId())
                .itemName(source.getItem().getName())
                .quantity(source.getQuantity())
                .price(source.getPrice())
                .build();
    }
}
