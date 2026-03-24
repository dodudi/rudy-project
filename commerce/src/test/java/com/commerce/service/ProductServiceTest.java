package com.commerce.service;

import com.commerce.domain.Member;
import com.commerce.domain.Product;
import com.commerce.dto.ProductCreateRequest;
import com.commerce.dto.ProductResponse;
import com.commerce.exception.DuplicateException;
import com.commerce.exception.NotFoundException;
import com.commerce.repository.MemberRepository;
import com.commerce.repository.ProductRepository;
import org.apache.logging.log4j.util.Strings;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
                .username("members")
                .nickname("member")
                .password("@password1234")
                .build()
        );
    }


    @Test
    void 제품생성_성공() {
        // given
        Long memberId = member.getId();
        String name = "test";
        String description = "test description";
        int price = 1000;
        int stock = 10;

        ProductCreateRequest request = new ProductCreateRequest(memberId, name, description, price, stock);

        // when
        ProductResponse product = productService.createProduct(request);

        // then
        Assertions.assertThat(product.name()).isEqualTo(name);
        Assertions.assertThat(product.description()).isEqualTo(description);
        Assertions.assertThat(product.price()).isEqualTo(price);
        Assertions.assertThat(product.stock()).isEqualTo(stock);
        Assertions.assertThat(product.nickname()).isEqualTo(member.getNickname());
    }

    @Test
    void 중복_제품이름_에러발생() {
        // given
        Long memberId = member.getId();
        String name = "test";
        String description = "test description";
        int price = 1000;
        int stock = 10;

        productRepository.save(Product.builder()
                .member(member)
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .build());

        ProductCreateRequest request = new ProductCreateRequest(memberId, name, description, price, stock);

        // when & then
        Assertions.assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(DuplicateException.class)
                .hasMessageContaining("이미 사용 중인 상품 이름입니다");
    }

    @Test
    void 제품생성_유효성_에러발생() {
        // given
        Long memberId = member.getId();
        String name = "test";
        String description = "test description";
        int price = 1000;
        int stock = 10;

        ProductCreateRequest validMember = new ProductCreateRequest(null, name, description, price, stock);
        ProductCreateRequest validName = new ProductCreateRequest(memberId, "", description, price, stock);
        ProductCreateRequest validDescription = new ProductCreateRequest(memberId, name, Strings.repeat(description, 50), price, stock);
        ProductCreateRequest validPrice = new ProductCreateRequest(memberId, name, description, -1, stock);
        ProductCreateRequest validStock = new ProductCreateRequest(memberId, name, description, price, -1);

        // when & then
        Assertions.assertThatThrownBy(() -> productService.createProduct(validMember))
                .isInstanceOf(InvalidDataAccessApiUsageException.class);

        // when & then
        Assertions.assertThatThrownBy(() -> productService.createProduct(validName))
                .isInstanceOf(IllegalArgumentException.class);

        // when & then
        Assertions.assertThatThrownBy(() -> productService.createProduct(validDescription))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품 설명은 최대 500자까지 입력 가능합니다.");

        // when & then
        Assertions.assertThatThrownBy(() -> productService.createProduct(validPrice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품 가격은 0원 이상 1억 원 이하만 가능합니다.");

        // when & then
        Assertions.assertThatThrownBy(() -> productService.createProduct(validStock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("재고 수량은 0개 이상 1,000만 개 이하만 가능합니다.");
    }

    @Test
    void 제품생성_존재하지_않는_회원_에러발생() {
        // given
        Long memberId = 3333L;
        String name = "test";
        String description = "test description";
        int price = 1000;
        int stock = 10;

        ProductCreateRequest request = new ProductCreateRequest(memberId, name, description, price, stock);

        // when & then
        Assertions.assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다");
    }
}