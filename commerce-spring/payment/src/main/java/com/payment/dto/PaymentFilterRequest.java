package com.payment.dto;

import com.payment.validation.ValidDateRange;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

@ValidDateRange
public record PaymentFilterRequest(
        @Min(value = 1, message = "회원 ID는 1 이상이어야 합니다.")
        Long memberId,

        @Min(value = 1, message = "주문 ID는 1 이상이어야 합니다.")
        Long orderId,

        @Pattern(regexp = "^(PENDING|COMPLETED|FAILED|REFUNDED)?$",
                 message = "상태는 PENDING, COMPLETED, FAILED, REFUNDED 중 하나여야 합니다.")
        String status,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant endDate,

        @Min(value = 0, message = "최소 금액은 0원 이상이어야 합니다.")
        Integer minAmount,

        @Min(value = 0, message = "최대 금액은 0원 이상이어야 합니다.")
        Integer maxAmount
) {}
