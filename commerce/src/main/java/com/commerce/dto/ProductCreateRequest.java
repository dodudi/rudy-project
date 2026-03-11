package com.commerce.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProductCreateRequest {

    @NotBlank
    private final String name;

    @Size(max = 500)
    private final String description;

    @Min(1000)
    @Max(100000)
    private final int price;

    @Min(1)
    @Max(10000)
    private final int stock;

}
