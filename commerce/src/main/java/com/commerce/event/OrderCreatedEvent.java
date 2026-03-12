package com.commerce.event;

public record OrderCreatedEvent(Long orderId, Long memberId, int totalAmount) {
}
