package com.project.deliveryservice.domain.order.service;

import com.project.deliveryservice.common.entity.Address;
import com.project.deliveryservice.domain.delivery.entity.Delivery;
import com.project.deliveryservice.domain.delivery.repository.DeliveryRepository;
import com.project.deliveryservice.domain.order.dto.OrderInfo;
import com.project.deliveryservice.domain.order.dto.OrderRequest;
import com.project.deliveryservice.domain.order.entity.Order;
import com.project.deliveryservice.domain.order.entity.OrderItem;
import com.project.deliveryservice.domain.order.repository.OrderRepository;
import com.project.deliveryservice.domain.user.entity.User;
import com.project.deliveryservice.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;

    private final UserService userService;
    private final OrderItemService orderItemService;

    @Transactional
    public OrderInfo createOrder(long userId, OrderRequest request) {
        // userId 검증
        User user = userService.getUserOrThrowById(userId);

        // itemId 검증 후 OrderItem 생성
        List<OrderItem> orderItems = request.getOrderItems().stream()
                .map(orderItemService::verifyAndCreateFromRequest)
                .toList();

        // 배달 정보 생성
        Address address = new Address(request.getCity(), request.getStreet(), request.getZipCode());
        Delivery delivery = Delivery.builder()
                .address(address)
                .build();

        // 주문 생성
        Order order = new Order(user, delivery, orderItems);
        return OrderInfo.of(orderRepository.save(order));
    }
}
