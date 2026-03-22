package com.commerce.ui.dto;

public record ProductCreateResponse(
        Long id,
        String nickname,
        String name,
        String description,
        int price,
        int stock
) {}
