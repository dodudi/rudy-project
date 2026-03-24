package com.commerce.dto;

import com.commerce.domain.Product;

public record ProductResponse(Long id, String nickname, String name, String description, int price, int stock) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getMember().getNickname(), product.getName(), product.getDescription(), product.getPrice(), product.getStock());
    }
}
