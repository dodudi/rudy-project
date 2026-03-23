package com.commerce.controller;

import com.commerce.dto.ProductCreateRequest;
import com.commerce.dto.ProductCreateResponse;
import com.commerce.dto.ProductFilterRequest;
import com.commerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductCreateResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @GetMapping
    public ResponseEntity<List<ProductCreateResponse>> getProducts(
            @Valid @ModelAttribute ProductFilterRequest filter
    ) {
        return ResponseEntity.ok(productService.getProducts(filter));
    }

}
