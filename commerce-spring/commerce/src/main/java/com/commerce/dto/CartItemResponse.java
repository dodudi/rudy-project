package com.commerce.dto;

import com.commerce.domain.CartItem;

public record CartItemResponse(
        Long id,
        String name,
        int quantity,
        int amount,
        int price
) {
    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(item.getId(), item.getProduct().getName(), item.getQuantity(), item.getAmount(), item.getPrice());
    }
}
