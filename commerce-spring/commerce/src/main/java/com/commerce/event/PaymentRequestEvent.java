package com.commerce.event;

public record PaymentRequestEvent(Long orderId, Long memberId, int amount) {

}
