package com.commerce.ui.dto;

import java.util.List;

public record OrderResponse(
        Long orderId,
        Long memberId,
        Long sellerId,
        String nickname,
        String status,
        int totalAmount,
        List<OrderItemResponse> orderItems,
        String createdAt
) {}
