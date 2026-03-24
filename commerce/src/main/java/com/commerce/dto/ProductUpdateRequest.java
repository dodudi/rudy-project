package com.commerce.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequest(
        @Size(min = 2, max = 20, message = "상품 이름은 2자 이상 20자 이하로 입력해야 합니다.")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣]*$", message = "상품 이름에 특수문자를 포함할 수 없습니다.")
        String name,

        @Size(max = 500, message = "상품 설명은 최대 500자까지 입력 가능합니다.")
        String description,

        @Min(value = 0, message = "상품 가격은 0원 이상이어야 합니다.")
        @Max(value = 100_000_000, message = "상품 가격은 1억 원 이하만 가능합니다.")
        Integer price,

        @Min(value = 0, message = "재고 수량은 0개 이상이어야 합니다.")
        @Max(value = 10_000_000, message = "재고 수량은 1,000만 개 이하만 가능합니다.")
        Integer stock
) {
}
