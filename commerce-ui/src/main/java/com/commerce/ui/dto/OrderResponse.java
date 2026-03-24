package com.commerce.ui.dto;

import java.util.List;

public record OrderResponse(
        Long orderId,
        String nickname,
        String status,
        int totalAmount,
        List<OrderItemResponse> orderItems,
        String createdAt
) {}
