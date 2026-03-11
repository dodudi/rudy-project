package com.commerce.service;

import com.commerce.domain.Product;
import com.commerce.dto.ProductCreateRequest;
import com.commerce.dto.ProductCreateResponse;
import com.commerce.repository.ProductRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;


    @Test
    void 제품생성_성공() {
        // given
        String name = "test";
        String description = "test description";
        int price = 1000;
        int stock = 10;

        ProductCreateRequest request = new ProductCreateRequest(name, description, price, stock);

        // when
        ProductCreateResponse product = productService.createProduct(request);

        // then
        Assertions.assertThat(product.getName()).isEqualTo(name);
        Assertions.assertThat(product.getDescription()).isEqualTo(description);
        Assertions.assertThat(product.getPrice()).isEqualTo(price);
        Assertions.assertThat(product.getStock()).isEqualTo(stock);
    }

    @Test
    void 중복_제품이름_에러발생() {
        // given
        String name = "test";
        String description = "test description";
        int price = 1000;
        int stock = 10;

        productRepository.save(Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .build());

        ProductCreateRequest request = new ProductCreateRequest(name, description, price, stock);


        // when & then
        Assertions.assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 상품입니다");
    }
}