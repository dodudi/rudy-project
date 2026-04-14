package com.commerce.ui.dto;

public record OrderItemResponse(
        Long productId,
        String productName,
        int price,
        int quantity,
        int amount
) {}
