package com.commerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequest(
        @NotNull(message = "주문하는 회원 인덱스 아이디는 필수 입력 값입니다.")
        Long memberId,

        @Valid
        @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다.")
        List<OrderItem> orderItems
) {
    public record OrderItem(
            @NotNull(message = "상품 인덱스 아이디는 필수 입력 값입니다.")
            Long productId,

            @Min(value = 1, message = "상품 수량은 1개 이상이어야 합니다.")
            int quantity
    ) {
    }
}
