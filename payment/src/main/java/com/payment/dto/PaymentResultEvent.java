package com.payment.dto;

public record PaymentResultEvent(Long orderId, String message, String status) {
}
