package com.commerce.dto;

import com.commerce.domain.OrderItem;

public record OrderItemResponse(
        Long productId,
        String productName,
        int price,
        int quantity,
        int amount
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getPrice(),
                item.getQuantity(),
                item.getAmount()
        );
    }
}
