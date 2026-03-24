package com.commerce.dto;

import com.commerce.domain.Order;
import com.commerce.domain.OrderStatus;

import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long orderId,
        String nickname,
        OrderStatus status,
        int totalAmount,
        List<OrderItemResponse> orderItems,
        Instant createdAt
) {
    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(OrderItemResponse::from)
                .toList();
        return new OrderResponse(order.getId(), order.getMember().getNickname(), order.getStatus(), order.getTotalAmount(), items, order.getCreatedAt());
    }
}
