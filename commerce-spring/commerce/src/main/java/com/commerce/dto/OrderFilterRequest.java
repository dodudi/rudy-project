package com.commerce.dto;

import com.commerce.domain.OrderStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

public record OrderFilterRequest(
        Long memberId,

        OrderStatus status,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant endDate
) {
}
