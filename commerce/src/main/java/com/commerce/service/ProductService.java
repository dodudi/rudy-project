package com.commerce.service;

import com.commerce.domain.Member;
import com.commerce.domain.Product;
import com.commerce.dto.ProductCreateRequest;
import com.commerce.dto.ProductFilterRequest;
import com.commerce.dto.ProductResponse;
import com.commerce.dto.ProductUpdateRequest;
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
    public ProductResponse createProduct(ProductCreateRequest request) {

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

        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return productRepository.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOTFOUND_PRODUCT));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts(ProductFilterRequest filter) {
        if (filter.minPrice() > 0 && filter.maxPrice() > 0 && filter.minPrice() > filter.maxPrice()) {
            throw new CommerceException(ErrorCode.INVALID_PRICE_RANGE);
        }

        return productRepository.findAll(ProductSpecification.withFilters(filter)).stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOTFOUND_PRODUCT));

        product.update(request.name(), request.description(), request.price(), request.stock());
        return ProductResponse.from(product);
    }
}
