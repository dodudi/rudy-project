package com.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotBlank(message = "결제키는 필수 값입니다.")
        String paymentKey,

        @NotNull(message = "주문ID는 필수 값입니다.")
        Long orderId,

        Long memberId,

        @NotBlank(message = "토스 전송용 주문ID는 필수입니다.")
        String tossOrderId,

        @Min(value = 0, message = "결제 금액은 0원 이상이어야 합니다.")
        int amount
) {
}
