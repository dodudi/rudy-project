package com.payment.dto;

import com.payment.domain.Payment;

import java.time.Instant;

public record PaymentResponse(Long id, Long orderId, int amount, String status, Instant createdAt) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCreatedAt()
        );
    }
}
