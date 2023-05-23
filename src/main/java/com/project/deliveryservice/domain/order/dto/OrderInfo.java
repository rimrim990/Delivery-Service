package com.project.deliveryservice.domain.order.dto;

import com.project.deliveryservice.domain.order.entity.Order;
import com.project.deliveryservice.domain.order.entity.OrderStatus;
import com.project.deliveryservice.domain.shop.dto.ShopInfo;
import com.project.deliveryservice.domain.user.dto.UserInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class OrderInfo {

    // 주문 아이디
    private Long orderId;

    // 주문한 가게
    private ShopInfo shop;

    // 주문자
    private UserInfoDto user;

    // 주문 아이템
    private List<OrderItemInfo> orderItems;

    // 주문 상태
    private OrderStatus orderStatus;

    // 주문 생성 시성
    private LocalDateTime createdAt;

    public static OrderInfo of(Order source) {
        List<OrderItemInfo> orderItems = source.getOrderItems().stream()
                .map(OrderItemInfo::of)
                .collect(Collectors.toList());
        return OrderInfo.builder()
                .orderId(source.getId())
                .user(UserInfoDto.of(source.getUser()))
                .orderItems(orderItems)
                .createdAt(source.getCreatedAt())
                .build();
    }
}
