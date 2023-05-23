package com.project.deliveryservice.domain.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.Range;

@Getter
@Builder
@AllArgsConstructor
public class OrderItemRequest {

    @NotNull
    private int itemId;

    @NotNull
    private int price;

    @NotNull
    @Range(min=1, max=999)
    private int quantity;
}
