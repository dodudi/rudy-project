package com.commerce.dto;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;

public record ProductFilterRequest(
        @Size(max = 100, message = "상품명은 최대 100자까지 입력 가능합니다.")
        String name,

        @Range(min = 0, max = 100_000_000, message = "최대 가격은 0원 이상 1억원 이하로 설정해야 합니다.")
        int maxPrice,

        @Range(min = 0, max = 100_000_000, message = "최소 가격은 0원 이상 1억원 이하로 설정해야 합니다.")
        int minPrice,

        boolean hasStock
) {
}
