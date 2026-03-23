package com.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RefundRequest(
        @NotNull(message = "주문ID는 필수 값입니다")
        Long orderId,

        @NotNull(message = "환불 이유는 필수 값입니다")
        @Size(min = 0, max = 200, message = "환불 이유는 0자 이상 200자 이하입니다")
        String reason
) {
}
