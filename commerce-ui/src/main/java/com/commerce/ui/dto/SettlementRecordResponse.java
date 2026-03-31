package com.commerce.ui.dto;

public record SettlementRecordResponse(
        Long id,
        Long orderId,
        Long sellerId,
        String paymentKey,
        int grossAmount,
        String status,
        String settlementDate,
        String settledAt,
        String createdAt
) {}
