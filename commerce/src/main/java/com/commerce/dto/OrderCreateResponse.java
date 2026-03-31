package com.commerce.dto;

import com.commerce.domain.Order;
import com.commerce.domain.OrderStatus;

import java.time.Instant;
import java.util.List;

public record OrderCreateResponse(
        Long orderId,
        Long memberId,
        Long sellerId,
        OrderStatus status,
        int totalAmount,
        List<OrderItemResponse> orderItems,
        Instant createdAt
) {

    // 정적 생성 팩토리 메서드
    public static OrderCreateResponse from(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(OrderItemResponse::from)
                .toList();

        // 전체 주문 제품 비용
        int totalAmount = items.stream()
                .mapToInt(OrderItemResponse::amount)
                .sum();

        return new OrderCreateResponse(
                order.getId(),
                order.getMember().getId(),
                order.getSellerId(),
                order.getStatus(),
                totalAmount,
                items,
                order.getCreatedAt()
        );
    }


}
