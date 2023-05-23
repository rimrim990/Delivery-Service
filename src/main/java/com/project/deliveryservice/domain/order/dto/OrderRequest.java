package com.project.deliveryservice.domain.order.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class OrderRequest {

    // 주문한 아이템 목록
    @NotEmpty
    List<OrderItemRequest> orderItems;

    // 주문 배송 주소
    @NotEmpty
    private String city;

    @NotEmpty
    private String street;

    @NotEmpty
    @Digits(integer = 5, fraction = 0, message = "must only contain numeric value")
    private String zipCode;
}
