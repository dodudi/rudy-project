package com.settlement.event;

import java.time.Instant;

public record PaymentResultEvent(
        Long orderId,
        String paymentKey,
        int amount,
        Long sellerId,
        String status,
        Instant completedAt
) {}
