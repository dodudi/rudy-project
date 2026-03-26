package com.commerce.dto;

import com.commerce.domain.Cart;

import java.util.List;

public record CartResponse(
        Long id,
        String nickname,
        List<CartItemResponse> items

) {
    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getCartItems().stream()
                .map(CartItemResponse::from)
                .toList();
        
        return new CartResponse(cart.getId(), cart.getMember().getNickname(), items);
    }
}
