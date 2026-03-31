package com.settlement.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SettlementTriggerRequest(
        @NotNull(message = "정산 기준일은 필수 값입니다.")
        LocalDate settlementDate
) {}
