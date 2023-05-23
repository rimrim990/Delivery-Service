package com.project.deliveryservice.domain.order.service;

import com.project.deliveryservice.domain.item.repository.ItemRepository;
import com.project.deliveryservice.domain.order.dto.OrderItemRequest;
import com.project.deliveryservice.domain.order.entity.OrderItem;
import com.project.deliveryservice.domain.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;

    public OrderItem verifyAndCreateFromRequest(OrderItemRequest request) {
        // itemId 검증
        itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("item not exist"));

        // OrderItemRequest -> OrderItem 생성
        return OrderItem.builder()
                .price(request.getPrice())
                .price(request.getQuantity())
                .build();
    }
}
