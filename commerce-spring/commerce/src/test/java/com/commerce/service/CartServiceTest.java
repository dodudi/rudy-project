package com.commerce.service;

import com.commerce.domain.Member;
import com.commerce.domain.Product;
import com.commerce.dto.CartCreateRequest;
import com.commerce.dto.CartResponse;
import com.commerce.event.PaymentRequestEvent;
import com.commerce.exception.NotFoundException;
import com.commerce.repository.MemberRepository;
import com.commerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private KafkaTemplate<String, PaymentRequestEvent> kafkaTemplate;

    private Member member;
    private Product productA;
    private Product productB;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
                .username("buyer")
                .password("@password1234")
                .nickname("buyer")
                .build());

        Member seller = memberRepository.save(Member.builder()
                .username("seller")
                .password("@password1234")
                .nickname("seller")
                .build());

        productA = productRepository.save(Product.builder()
                .member(seller)
                .name("상품A")
                .description("상품A 설명")
                .price(10000)
                .stock(100)
                .build());

        productB = productRepository.save(Product.builder()
                .member(seller)
                .name("상품B")
                .description("상품B 설명")
                .price(20000)
                .stock(200)
                .build());
    }

    @Test
    void 장바구니_생성_성공() {
        // given
        CartCreateRequest request = new CartCreateRequest(member.getId(), List.of(
                new CartCreateRequest.ProductItem(productA.getId(), 2),
                new CartCreateRequest.ProductItem(productB.getId(), 3)
        ));

        // when
        CartResponse response = cartService.createCart(request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.nickname()).isEqualTo(member.getNickname());
        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).name()).isEqualTo(productA.getName());
        assertThat(response.items().get(1).name()).isEqualTo(productB.getName());
    }

    @Test
    void 장바구니_생성_금액_정상계산() {
        // given
        int quantityA = 3;
        int quantityB = 5;

        CartCreateRequest request = new CartCreateRequest(member.getId(), List.of(
                new CartCreateRequest.ProductItem(productA.getId(), quantityA),
                new CartCreateRequest.ProductItem(productB.getId(), quantityB)
        ));

        // when
        CartResponse response = cartService.createCart(request);

        // then
        assertThat(response.items().get(0).amount()).isEqualTo(productA.getPrice() * quantityA);
        assertThat(response.items().get(1).amount()).isEqualTo(productB.getPrice() * quantityB);
    }

    @Test
    void 장바구니_생성_존재하지않는_회원_에러발생() {
        // given
        CartCreateRequest request = new CartCreateRequest(-1L, List.of(
                new CartCreateRequest.ProductItem(productA.getId(), 1)
        ));

        // when & then
        assertThatThrownBy(() -> cartService.createCart(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다");
    }

    @Test
    void 장바구니_생성_존재하지않는_상품_에러발생() {
        // given
        CartCreateRequest request = new CartCreateRequest(member.getId(), List.of(
                new CartCreateRequest.ProductItem(-1L, 1)
        ));

        // when & then
        assertThatThrownBy(() -> cartService.createCart(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 상품입니다");
    }
}
