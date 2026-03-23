package com.payment.dto;

public record TossConfirmResponse(
        String paymentKey,
        String orderId,
        String status,
        int totalAmount
) {
}
