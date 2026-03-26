package com.commerce.service;

import com.commerce.domain.Cart;
import com.commerce.domain.Member;
import com.commerce.domain.Product;
import com.commerce.dto.CartCreateRequest;
import com.commerce.dto.CartResponse;
import com.commerce.exception.ErrorCode;
import com.commerce.exception.NotFoundException;
import com.commerce.repository.CartRepository;
import com.commerce.repository.MemberRepository;
import com.commerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartResponse createCart(CartCreateRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOTFOUND_USER));

        Cart cart = cartRepository.save(Cart.builder()
                .member(member)
                .build());

        List<Long> productIds = request.products().stream()
                .map(CartCreateRequest.ProductItem::productId)
                .toList();

        Map<Long, Product> productMap = productRepository.findAllWithLockByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        for (CartCreateRequest.ProductItem productItem : request.products()) {
            Product product = Optional.ofNullable(productMap.get(productItem.productId()))
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOTFOUND_PRODUCT));

            cart.addCartItem(product, productItem.quantity());
        }

        return CartResponse.from(cart);
    }

}
