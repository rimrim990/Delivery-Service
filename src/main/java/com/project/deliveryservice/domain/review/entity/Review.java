package com.project.deliveryservice.domain.review.entity;

import com.project.deliveryservice.common.entity.ExtendedTimeEntity;
import com.project.deliveryservice.domain.order.entity.Order;
import com.project.deliveryservice.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends ExtendedTimeEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ColumnDefault(value = "false")
    @Column(nullable = false)
    // 리뷰 공개 여부
    private boolean isPublic;

    @Column(nullable = true)
    private String photoUrl;

    @Size(max=100)
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    // 별점. 1~5 사이의 정수 값
    private int star;
}
