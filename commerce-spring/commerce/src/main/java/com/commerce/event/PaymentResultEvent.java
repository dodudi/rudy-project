package com.commerce.event;

import com.commerce.domain.OrderStatus;

public record PaymentResultEvent(Long orderId, String message, OrderStatus status, Long sellerId) {
}
