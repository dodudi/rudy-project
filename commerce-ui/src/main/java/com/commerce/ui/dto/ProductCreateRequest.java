package com.commerce.ui.dto;

import jakarta.validation.constraints.*;

public class ProductCreateRequest {

    @NotNull(message = "회원 ID를 입력해주세요")
    private Long memberId;

    @NotBlank(message = "상품명을 입력해주세요")
    @Size(min = 2, max = 20, message = "상품명은 2~20자여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]*$", message = "상품명은 영문, 숫자, 한글만 가능합니다")
    private String name;

    @Size(max = 500, message = "설명은 최대 500자까지 입력 가능합니다")
    private String description;

    @Min(value = 0, message = "가격은 0원 이상이어야 합니다")
    @Max(value = 100000000, message = "가격은 1억원 이하여야 합니다")
    private int price;

    @Min(value = 0, message = "재고는 0개 이상이어야 합니다")
    @Max(value = 10000000, message = "재고는 1천만개 이하여야 합니다")
    private int stock;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}
