package com.project.deliveryservice.domain.order.entity;

public enum OrderStatus {
    REQUESTED, // 주문 요청
    PROGRESS, // 주문 처리 중
    CANCELED, // 주문 취소
    COMPLETED, // 주문 완료
    REFUND_REQUESTED, // 환불 요청
    REFUND_COMPLETED // 환불 완료
}
