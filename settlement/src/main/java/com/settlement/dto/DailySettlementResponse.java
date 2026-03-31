package com.settlement.dto;

import com.settlement.domain.DailySettlement;
import com.settlement.domain.DailySettlementStatus;

import java.time.Instant;
import java.time.LocalDate;

public record DailySettlementResponse(
        Long id,
        Long sellerId,
        LocalDate settlementDate,
        int orderCount,
        long totalAmount,
        DailySettlementStatus status,
        Instant createdAt
) {
    public static DailySettlementResponse from(DailySettlement settlement) {
        return new DailySettlementResponse(
                settlement.getId(),
                settlement.getSellerId(),
                settlement.getSettlementDate(),
                settlement.getOrderCount(),
                settlement.getTotalAmount(),
                settlement.getStatus(),
                settlement.getCreatedAt()
        );
    }
}
