package com.commerce.service;

import com.commerce.domain.Member;
import com.commerce.domain.OrderStatus;
import com.commerce.domain.Product;
import com.commerce.dto.OrderCreateRequest;
import com.commerce.dto.OrderCreateResponse;
import com.commerce.event.PaymentRequestEvent;
import com.commerce.repository.MemberRepository;
import com.commerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.commerce.event.PaymentRequestEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @MockitoBean
    private KafkaTemplate<String, PaymentRequestEvent> kafkaTemplate;

    private Member member;
    private Product radioProduct;
    private Product phoneProduct;

    @BeforeEach
    void setUp() {
        radioProduct = productRepository.save(Product.builder()
                .name("radio")
                .description("radio description")
                .price(50000)
                .stock(5000)
                .build()
        );

        phoneProduct = productRepository.save(Product.builder()
                .name("phone")
                .description("phone description")
                .price(10000)
                .stock(10000)
                .build()
        );

        member = memberRepository.save(Member.builder()
                .username("member")
                .nickname("member")
                .build()
        );
    }

    @Test
    void 주문_생성_성공() {
        // given
        int radioQuantity = 2;
        int radioAmount = radioProduct.getPrice() * radioQuantity;

        int phoneQuantity = 3;
        int phoneAmount = phoneProduct.getPrice() * phoneQuantity;

        List<OrderCreateRequest.OrderItem> items = List.of(
                new OrderCreateRequest.OrderItem(radioProduct.getId(), radioQuantity),
                new OrderCreateRequest.OrderItem(phoneProduct.getId(), phoneQuantity)
        );
        OrderCreateRequest request = new OrderCreateRequest(member.getId(), items);

        // when
        OrderCreateResponse response = orderService.createOrder(request);

        // then
        assertThat(response.orderId()).isNotNull();
        assertThat(response.memberId()).isEqualTo(member.getId());
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.orderItems()).hasSize(items.size());
        assertThat(response.totalAmount()).isEqualTo(radioAmount + phoneAmount);
    }

    @Test
    void 주문_재고_감소_적용() {
        // given
        int orderQuantity = 5;
        int availableStock = radioProduct.getStock();

        List<OrderCreateRequest.OrderItem> items = List.of(
                new OrderCreateRequest.OrderItem(radioProduct.getId(), orderQuantity)
        );
        OrderCreateRequest request = new OrderCreateRequest(member.getId(), items);

        // when
        orderService.createOrder(request);

        // then
        Product updated = productRepository.findById(radioProduct.getId()).orElseThrow();
        assertThat(updated.getStock()).isEqualTo(availableStock - orderQuantity);
    }

    @Test
    void 주문_재고_부족_에러_발생() {
        // given
        List<OrderCreateRequest.OrderItem> items = List.of(
                new OrderCreateRequest.OrderItem(radioProduct.getId(), 99999)
        );
        OrderCreateRequest request = new OrderCreateRequest(member.getId(), items);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재고 부족");
    }

    @Test
    void 주문_존재하지_않는_회원_에러_발생() {
        // given
        Long nonExistentMemberId = -1L;
        List<OrderCreateRequest.OrderItem> items = List.of(
                new OrderCreateRequest.OrderItem(radioProduct.getId(), 1)
        );
        OrderCreateRequest request = new OrderCreateRequest(nonExistentMemberId, items);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 회원");
    }

    @Test
    void 주문_존재하지_않는_상품_에러_발생() {
        // given
        Long nonExistentProductId = -1L;
        List<OrderCreateRequest.OrderItem> items = List.of(
                new OrderCreateRequest.OrderItem(nonExistentProductId, 1)
        );
        OrderCreateRequest request = new OrderCreateRequest(member.getId(), items);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 상품");
    }
}
