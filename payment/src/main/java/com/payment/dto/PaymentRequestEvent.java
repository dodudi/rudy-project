package com.payment.dto;

public record PaymentRequestEvent(Long orderId, Long memberId, int amount) {
}
