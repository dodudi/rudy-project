package com.commerce.service;

import com.commerce.domain.Product;
import com.commerce.dto.ProductCreateRequest;
import com.commerce.dto.ProductCreateResponse;
import com.commerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductCreateResponse createProduct(ProductCreateRequest request) {

        if (productRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("존재하지 않는 상품입니다");
        }

        Product product = productRepository.save(Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .build());

        return ProductCreateResponse.from(product);
    }
}
