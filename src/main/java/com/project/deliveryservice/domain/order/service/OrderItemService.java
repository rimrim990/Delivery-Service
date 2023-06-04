package com.project.deliveryservice.domain.order.service;

import com.project.deliveryservice.domain.item.repository.ItemRepository;
import com.project.deliveryservice.domain.order.dto.OrderItemRequest;
import com.project.deliveryservice.domain.order.entity.OrderItem;
import com.project.deliveryservice.domain.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final static int MAX_QUANTITY = 999;

    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;

    public OrderItem verifyAndCreateFromRequest(OrderItemRequest request) {
        // itemId 검증
        itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("item not exist"));

        // OrderItemRequest -> OrderItem 생성
        return OrderItem.builder()
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build();
    }

    public void throwIfExceedMaxQuantity(List<OrderItem> orderItems) {
        int totalQuantity = getTotalItemQuantity(orderItems);
        if (totalQuantity > MAX_QUANTITY)
            throw new IllegalArgumentException("exceed max quantity");
    }

    private int getTotalItemQuantity(List<OrderItem> orderItems) {
        return orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }
}
