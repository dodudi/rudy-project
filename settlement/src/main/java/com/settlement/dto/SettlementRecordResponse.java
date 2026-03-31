package com.settlement.dto;

import com.settlement.domain.SettlementRecord;
import com.settlement.domain.SettlementStatus;

import java.time.Instant;
import java.time.LocalDate;

public record SettlementRecordResponse(
        Long id,
        Long orderId,
        Long sellerId,
        String paymentKey,
        int grossAmount,
        SettlementStatus status,
        LocalDate settlementDate,
        Instant settledAt,
        Instant createdAt
) {
    public static SettlementRecordResponse from(SettlementRecord record) {
        return new SettlementRecordResponse(
                record.getId(),
                record.getOrderId(),
                record.getSellerId(),
                record.getPaymentKey(),
                record.getGrossAmount(),
                record.getStatus(),
                record.getSettlementDate(),
                record.getSettledAt(),
                record.getCreatedAt()
        );
    }
}
