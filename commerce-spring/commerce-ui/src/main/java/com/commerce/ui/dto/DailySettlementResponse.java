package com.commerce.ui.dto;

public record DailySettlementResponse(
        Long id,
        Long sellerId,
        String settlementDate,
        int orderCount,
        long totalAmount,
        String status,
        String createdAt
) {}
