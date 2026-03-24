package com.commerce.ui.dto;

public record PaymentResponse(
        Long id,
        Long orderId,
        Long memberId,
        int amount,
        String status,
        String createdAt
) {}
