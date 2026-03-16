package com.payment.event;

public record PaymentRequestEvent(Long orderId, Long memberId, int amount) {
}
