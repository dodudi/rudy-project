package com.commerce.dto;

import com.commerce.domain.Product;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProductCreateResponse {

    private final Long id;
    private final String name;
    private final String description;
    private final int price;
    private final int stock;

    public static ProductCreateResponse from(Product product) {
        return new ProductCreateResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice(), product.getStock());
    }
}
