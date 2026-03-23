package com.commerce.service;

import com.commerce.domain.Member;
import com.commerce.domain.Product;
import com.commerce.dto.ProductCreateRequest;
import com.commerce.dto.ProductCreateResponse;
import com.commerce.dto.ProductFilterRequest;
import com.commerce.exception.CommerceException;
import com.commerce.exception.DuplicateException;
import com.commerce.exception.ErrorCode;
import com.commerce.exception.NotFoundException;
import com.commerce.repository.MemberRepository;
import com.commerce.repository.ProductRepository;
import com.commerce.repository.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ProductCreateResponse createProduct(ProductCreateRequest request) {

        if (productRepository.existsByName(request.name())) {
            throw new DuplicateException(ErrorCode.DUPLICATE_PRODUCT_NAME);
        }

        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOTFOUND_USER));

        Product product = productRepository.save(Product.builder()
                .member(member)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .build());

        return ProductCreateResponse.from(product);
    }

    @Transactional(readOnly = true)
    public List<ProductCreateResponse> getProducts(ProductFilterRequest filter) {
        if (filter.minPrice() > 0 && filter.maxPrice() > 0 && filter.minPrice() > filter.maxPrice()) {
            throw new CommerceException(ErrorCode.INVALID_PRICE_RANGE);
        }

        return productRepository.findAll(ProductSpecification.withFilters(filter)).stream()
                .map(ProductCreateResponse::from)
                .toList();
    }
}
