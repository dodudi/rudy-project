package com.commerce.controller;

import com.commerce.dto.CartCreateRequest;
import com.commerce.dto.CartResponse;
import com.commerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartResponse> createCart(
            @Valid @RequestBody CartCreateRequest request
    ) {
        CartResponse cart = cartService.createCart(request);
        return ResponseEntity.ok(cart);
    }
}
