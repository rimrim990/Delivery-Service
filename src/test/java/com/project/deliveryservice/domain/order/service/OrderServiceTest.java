package com.project.deliveryservice.domain.order.service;

import com.project.deliveryservice.domain.delivery.service.DeliveryService;
import com.project.deliveryservice.domain.item.entity.Item;
import com.project.deliveryservice.domain.order.dto.OrderInfo;
import com.project.deliveryservice.domain.order.dto.OrderItemRequest;
import com.project.deliveryservice.domain.order.dto.OrderRequest;
import com.project.deliveryservice.domain.order.repository.OrderRepository;
import com.project.deliveryservice.domain.user.entity.User;
import com.project.deliveryservice.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    OrderRepository mockOrderRepository;
    DeliveryService mockDeliveryService;
    UserService mockUserService;
    OrderItemService mockOrderItemService;

    OrderService orderService;

    private static final int MAX_ITEM_LIMIT = 999;

    @BeforeEach
    void setup() {
        mockOrderRepository = Mockito.mock(OrderRepository.class);
        mockDeliveryService = Mockito.mock(DeliveryService.class);
        mockUserService = Mockito.mock(UserService.class);
        mockOrderItemService = Mockito.mock(OrderItemService.class);
        orderService = new OrderService(mockOrderRepository, mockDeliveryService, mockUserService, mockOrderItemService);
    }

    Item getItem(long itemId, int price) {
        return new Item(itemId, null, "test", "test", price, false);
    }

    OrderItemRequest getOrderItemRequest(long itemId, int price, int quantity) {
        return new OrderItemRequest(itemId, price, quantity);
    }

    OrderRequest getOrderRequest(List<OrderItemRequest> orderItemRequests) {
        return new OrderRequest(orderItemRequests, "seoul", "songpa", "12345");
    }

    @Test
    @DisplayName("주문을 요청한 아이템의 아이디가 유효하지 않으면 예외를 던진다.")
    void test_createOrder_01() {

        // given
        List<OrderItemRequest> orderItemRequests = LongStream.rangeClosed(1, 10)
                .mapToObj(l -> getOrderItemRequest(l, (int)l*1000, (int)l))
                .collect(Collectors.toList());
        OrderRequest orderRequest = getOrderRequest(orderItemRequests);

        // when
        // Exception ! - 아이템 정보가 종재하지 않음

        // 주문 생성 - TODO: Exception 수정
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(1L, orderRequest));

        // then - TODO: 예외 메세지 수정
        assertEquals(throwable.getMessage(), "invalid item id");
    }

    @Test
    @DisplayName("주문을 요청한 사용자 정보가 유효하지 않으면 예외를 던진다.")
    void test_createOrder_02() {

        // given
        long invalidUserId = 1L;
        List<OrderItemRequest> orderItemRequests = LongStream.rangeClosed(1, 10)
                .mapToObj(l -> getOrderItemRequest(l, (int)l*1000, (int)l))
                .collect(Collectors.toList());
        OrderRequest orderRequest = getOrderRequest(orderItemRequests);

        // when

        // Exception ! - 사용자 정보가 존재하지 않음
        when(mockUserService.getUserOrThrowById(invalidUserId))
                .thenThrow(new IllegalArgumentException("invalid user id"));

        // 주문 생성 - TODO: Exception 수정
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(invalidUserId, orderRequest));

        // then - TODO: 예외 메세지 수정
        assertEquals(throwable.getMessage(), "invalid user id");
    }

    @Test
    @DisplayName("주문 요청시에 아이템의 수가 상한 값을 넘으면 예외를 던진다.")
    void test_createOrder_03() {
        // given
        long validUserId = 1L;
        long validItemId = 1L;
        List<OrderItemRequest> orderItemRequests = List.of(
                getOrderItemRequest(validItemId, 1000, MAX_ITEM_LIMIT+1)
        );
        OrderRequest orderRequest = getOrderRequest(orderItemRequests);

        // when
        when(mockUserService.getUserOrThrowById(validUserId)).thenReturn(
                User.builder().build()
        );

        // 주문 생성 - TODO: Exception 수정
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(validUserId, orderRequest));

        // then - TODO: 예외 메세지 수정
        assertEquals(throwable.getMessage(), "max limit");
    }

    @Test
    @DisplayName("주문 요청 시에 수량이 0인 아이템은 집계하지 않는다.")
    void test_createOrder_04() {

        // given
        long validUserId = 1L;
        List<OrderItemRequest> orderItemRequests = List.of(
                getOrderItemRequest(1L, 1000, 10),
                getOrderItemRequest(2L, 2000, 0)
        );
        OrderRequest orderRequest = getOrderRequest(orderItemRequests);

        // when
        when(mockUserService.getUserOrThrowById(validUserId))
                .thenReturn(User.builder().build());

        // then
        OrderInfo dto = orderService.createOrder(validUserId, orderRequest);
        assertEquals(dto.getOrderItems().size(),1);
    }

    @Test
    @DisplayName("주문 요청 시에 총 주문 수량의 합이 0일 경우 예외를 던진다.")
    void test_createOrder_05() {

        // given - orderItems 의 quantity 합이 0
        List<OrderItemRequest> orderItemRequests = LongStream.rangeClosed(1, 10)
                .mapToObj(l -> getOrderItemRequest(l, (int) l * 1000, 0))
                .collect(Collectors.toList());
        OrderRequest orderRequest = getOrderRequest(orderItemRequests);

        // when
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(1L, orderRequest));

        // then
        assertEquals(throwable.getMessage(), "unavailable");
    }

    @Test
    @DisplayName("주문 요청 시에 주문 리스트가 비어 있을 경우 예외를 던진다.")
    void test_createOrder_06() {

        // given - orderItems 이 empty
        List<OrderItemRequest> orderItemRequests = new ArrayList<>();
        OrderRequest orderRequest = getOrderRequest(orderItemRequests);

        // when
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(1L, orderRequest));

        // then
        assertEquals(throwable.getMessage(), "unavailable");
    }

    @Test
    @DisplayName("주문 요청에 성공하면 orderInfoDto 를 반환한다.")
    void test_createOrder_07() {

        // given
        long validUserId = 1L;
        List<Item> items = LongStream.rangeClosed(1, 10)
                .mapToObj(l -> getItem(l, (int) l * 1000)).toList();
        List<OrderItemRequest> orderItemRequests = items.stream()
                .map(i -> getOrderItemRequest(i.getId(), i.getPrice(), 1))
                .collect(Collectors.toList());
        OrderRequest orderRequest = getOrderRequest(orderItemRequests);

        // when
        when(mockUserService.getUserOrThrowById(validUserId))
                .thenReturn(User.builder().build());

        // then
        OrderInfo dto = orderService.createOrder(validUserId, orderRequest);
        assertEquals(dto.getOrderItems(), List.of());
    }

}