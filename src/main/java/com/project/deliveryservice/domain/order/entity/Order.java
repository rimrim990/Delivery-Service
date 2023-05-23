package com.project.deliveryservice.domain.order.entity;

import com.project.deliveryservice.common.entity.ExtendedTimeEntity;
import com.project.deliveryservice.domain.delivery.entity.Delivery;
import com.project.deliveryservice.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Order extends ExtendedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    // 환불 요청 시각
    private LocalDateTime refundRequestedAt;

    // 환불 완료 시각
    private LocalDateTime refundCompletedAt;

    // 주문 처리 완료 시각
    private LocalDateTime completedAt;

    // 주문 취소 시각
    private LocalDateTime canceledAt;

    public void setOrderItems(List<OrderItem> orderItems) {
        orderItems.forEach(oi -> oi.setOrder(this));
        this.orderItems = orderItems;
    }
}
