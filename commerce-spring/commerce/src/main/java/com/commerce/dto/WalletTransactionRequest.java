package com.commerce.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WalletTransactionRequest(
        @NotNull(message = "금액은 필수 값입니다")
        @Positive(message = "금액은 0원 초과이어야 합니다")
        Long amount
) {
}
