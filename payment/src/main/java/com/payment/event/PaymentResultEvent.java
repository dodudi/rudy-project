package com.payment.event;

import java.time.Instant;

public record PaymentResultEvent(Long orderId, String message, String status, Long sellerId,
                                  String paymentKey, int amount, Instant completedAt) {

    public static PaymentResultEvent completed(Long orderId, Long sellerId, String paymentKey, int amount) {
        return new PaymentResultEvent(orderId, "결제 승인 완료", "COMPLETED", sellerId, paymentKey, amount, Instant.now());
    }

    public static PaymentResultEvent failed(Long orderId, String reason) {
        return new PaymentResultEvent(orderId, reason, "FAILED", null, null, 0, null);
    }

    public static PaymentResultEvent refunded(Long orderId, Long sellerId, String paymentKey, int amount) {
        return new PaymentResultEvent(orderId, "환불 완료", "REFUNDED", sellerId, paymentKey, amount, Instant.now());
    }
}
