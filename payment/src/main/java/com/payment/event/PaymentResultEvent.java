package com.payment.event;

public record PaymentResultEvent(Long orderId, String message, String status) {

    public static PaymentResultEvent completed(Long orderId) {
        return new PaymentResultEvent(orderId, "결제 승인 완료", "COMPLETED");
    }

    public static PaymentResultEvent failed(Long orderId, String reason) {
        return new PaymentResultEvent(orderId, reason, "FAILED");
    }
}
