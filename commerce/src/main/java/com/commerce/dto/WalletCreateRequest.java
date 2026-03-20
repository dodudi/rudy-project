package com.commerce.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record WalletCreateRequest(
        @NotNull(message = "회원 아이디는 필수 값입니다")
        Long memberId,

        @Range(min = 0, max = 100_000_000, message = "잔고 금액은 0원 이상 1억 원 이하만 가능합니다")
        Long balance
) {
}
