package com.payment.event;

public record PaymentResultEvent(Long orderId, String message, String status) {
}
