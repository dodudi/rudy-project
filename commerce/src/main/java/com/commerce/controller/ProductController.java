package com.commerce.controller;

import com.commerce.dto.ProductCreateRequest;
import com.commerce.dto.ProductCreateResponse;
import com.commerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductCreateResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductCreateResponse product = productService.createProduct(request);
        return ResponseEntity.ok(product);
    }

}
