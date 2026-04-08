package com.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TestPaymentRequest(
        @NotNull(message = "주문ID는 필수 값입니다.")
        Long orderId,

        @NotNull(message = "회원ID는 필수 값입니다.")
        Long memberId,

        Long sellerId,

        @Min(value = 0, message = "결제 금액은 0원 이상이어야 합니다.")
        int amount
) {
}
