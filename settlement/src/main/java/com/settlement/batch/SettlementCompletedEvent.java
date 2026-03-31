package com.settlement.batch;

import java.time.Instant;
import java.time.LocalDate;

public record SettlementCompletedEvent(
        LocalDate settlementDate,
        int totalOrderCount,
        long totalAmount,
        String jobId,
        Instant completedAt
) {}
