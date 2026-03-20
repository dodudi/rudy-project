package com.commerce.dto;

import com.commerce.domain.Product;

public record ProductCreateResponse(Long id, String nickname, String name, String description, int price, int stock) {
    public static ProductCreateResponse from(Product product) {
        return new ProductCreateResponse(product.getId(), product.getMember().getNickname(), product.getName(), product.getDescription(), product.getPrice(), product.getStock());
    }
}
