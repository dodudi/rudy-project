package com.commerce.dto;

import jakarta.validation.constraints.*;

public record ProductCreateRequest(
        @NotNull(message = "등록 회원 인덱스 아이디는 필수 입력 값입니다.")
        Long memberId,

        @NotBlank(message = "상품 이름은 필수 입력 값입니다.")
        @Size(min = 2, max = 20, message = "상품 이름은 2자 이상 20자 이하로 입력해야 합니다.")
        @Pattern(
                regexp = "^[a-zA-Z0-9가-힣]*$",
                message = "상품 이름에 특수문자를 포함할 수 없습니다."
        )
        String name,

        @Size(max = 500, message = "상품 설명은 최대 500자까지 입력 가능합니다.")
        String description,

        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        @Max(value = 100_000_000, message = "가격은 1억 원을 초과할 수 없습니다.")
        int price,

        @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
        @Max(value = 10_000_000, message = "재고는 1,000만 개를 초과할 수 없습니다.")
        int stock
) {
}
