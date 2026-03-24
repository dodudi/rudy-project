package com.commerce.service;

import com.commerce.domain.Member;
import com.commerce.domain.OrderStatus;
import com.commerce.domain.Product;
import com.commerce.dto.OrderCreateRequest;
import com.commerce.dto.OrderCreateResponse;
import com.commerce.dto.OrderFilterRequest;
import com.commerce.dto.OrderResponse;
import com.commerce.event.PaymentRequestEvent;
import com.commerce.exception.EmptyException;
import com.commerce.exception.InsufficientStockException;
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

import java.time.Instant;
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
        member = memberRepository.save(Member.builder()
                .username("member")
                .password("@password1234")
                .nickname("member")
                .build()
        );

        Member seller = memberRepository.save(Member.builder()
                .username("seller")
                .password("@password1234")
                .nickname("seller")
                .build());

        radioProduct = productRepository.save(Product.builder()
                .name("radio")
                .member(seller)
                .description("radio description")
                .price(50000)
                .stock(5000)
                .build()
        );

        phoneProduct = productRepository.save(Product.builder()
                .name("phone")
                .member(seller)
                .description("phone description")
                .price(10000)
                .stock(10000)
                .build()
        );
    }

    @Test
    void 주문생성_성공() {
        // given
        int radioQuantity = 100;
        int phoneQuantity = 200;

        OrderCreateRequest request = new OrderCreateRequest(member.getId(), List.of(
                new OrderCreateRequest.OrderItem(radioProduct.getId(), radioQuantity),
                new OrderCreateRequest.OrderItem(phoneProduct.getId(), phoneQuantity)
        ));


        // when
        OrderCreateResponse order = orderService.createOrder(request);

        // then
        assertThat(order.memberId()).isEqualTo(member.getId());
        assertThat(order.orderItems().size()).isEqualTo(2);
        assertThat(order.orderItems().get(0).productId()).isEqualTo(radioProduct.getId());
        assertThat(order.orderItems().get(1).productId()).isEqualTo(phoneProduct.getId());
        assertThat(order.orderItems().get(0).amount()).isEqualTo(radioProduct.getPrice() * radioQuantity);
        assertThat(order.orderItems().get(1).amount()).isEqualTo(phoneProduct.getPrice() * phoneQuantity);
    }

    @Test
    void 주문생성_재고감소_적용() {
        // given
        int orderQuantity = 5;
        int availableStock = radioProduct.getStock();

        OrderCreateRequest request = new OrderCreateRequest(member.getId(), List.of(
                new OrderCreateRequest.OrderItem(radioProduct.getId(), orderQuantity)
        ));

        // when
        orderService.createOrder(request);

        // then
        assertThat(radioProduct.getStock()).isEqualTo(availableStock - orderQuantity);
    }

    @Test
    void 주문생성_재고부족_에러발생() {
        // given
        OrderCreateRequest request = new OrderCreateRequest(member.getId(), List.of(
                new OrderCreateRequest.OrderItem(radioProduct.getId(), 99999)
        ));

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("재고가 부족합니다");
    }

    @Test
    void 주문생성_존재하지_않는회원_에러발생() {
        // given
        Long nonExistentMemberId = -1L;
        OrderCreateRequest request = new OrderCreateRequest(nonExistentMemberId, List.of(
                new OrderCreateRequest.OrderItem(radioProduct.getId(), 1)
        ));

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다");
    }

    @Test
    void 주문생성_존재하지_않는상품_에러발생() {
        // given
        Long nonExistentProductId = -1L;

        OrderCreateRequest request = new OrderCreateRequest(member.getId(), List.of(
                new OrderCreateRequest.OrderItem(nonExistentProductId, 1),
                new OrderCreateRequest.OrderItem(phoneProduct.getId(), 1),
                new OrderCreateRequest.OrderItem(radioProduct.getId(), 1)
        ));

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 상품입니다");
    }

    @Test
    void 주문생성_상품목록_없는경우_에러발생() {
        // given
        OrderCreateRequest request = new OrderCreateRequest(member.getId(), List.of());

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(EmptyException.class)
                .hasMessageContaining("상품 정보가 비었습니다");
    }

    @Test
    void 주문생성_구매상품_수량0개_에러발생() {
        // given
        OrderCreateRequest request = new OrderCreateRequest(member.getId(), List.of(
                new OrderCreateRequest.OrderItem(phoneProduct.getId(), 0)
        ));

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품 구매 수량은 1개 이상이어야 합니다.");
    }

    @Test
    void 주문목록_상태필터_조회성공() {
        // given
        orderService.createOrder(new OrderCreateRequest(member.getId(), List.of(
                new OrderCreateRequest.OrderItem(radioProduct.getId(), 1)
        )));

        OrderFilterRequest filterRequest = new OrderFilterRequest(member.getId(), OrderStatus.CREATED, null, null);

        // when
        List<OrderResponse> orders = orderService.getOrders(filterRequest);

        // then
        assertThat(orders).hasSize(1);
        assertThat(orders.getFirst().status()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void 주문목록_날짜범위_필터조회_성공() {
        // given
        orderService.createOrder(new OrderCreateRequest(member.getId(), List.of(
                new OrderCreateRequest.OrderItem(radioProduct.getId(), 1)
        )));

        Instant start = Instant.now().minusSeconds(60);
        Instant end = Instant.now().plusSeconds(60);
        OrderFilterRequest filterRequest = new OrderFilterRequest(member.getId(), null, start, end);

        // when
        List<OrderResponse> orders = orderService.getOrders(filterRequest);

        // then
        assertThat(orders).hasSize(1);
    }

    @Test
    void 주문목록_날짜범위_벗어난경우_빈목록반환() {
        // given
        orderService.createOrder(new OrderCreateRequest(member.getId(), List.of(
                new OrderCreateRequest.OrderItem(radioProduct.getId(), 1)
        )));

        Instant start = Instant.now().plusSeconds(3600);
        Instant end = Instant.now().plusSeconds(7200);
        OrderFilterRequest filterRequest = new OrderFilterRequest(member.getId(), null, start, end);

        // when
        List<OrderResponse> orders = orderService.getOrders(filterRequest);

        // then
        assertThat(orders).isEmpty();
    }

    @Test
    void 주문목록_조회성공() {
        // given
        OrderCreateRequest createRequest = new OrderCreateRequest(member.getId(), List.of(
                new OrderCreateRequest.OrderItem(radioProduct.getId(), 1),
                new OrderCreateRequest.OrderItem(phoneProduct.getId(), 2)
        ));
        OrderCreateResponse created = orderService.createOrder(createRequest);

        OrderFilterRequest filterRequest = new OrderFilterRequest(member.getId(), null, null, null);

        // when
        List<OrderResponse> orders = orderService.getOrders(filterRequest);

        // then
        assertThat(orders).hasSize(1);
        assertThat(orders.getFirst().orderId()).isEqualTo(created.orderId());
        assertThat(orders.getFirst().nickname()).isEqualTo(member.getNickname());
        assertThat(orders.getFirst().status()).isEqualTo(created.status());
        assertThat(orders.getFirst().orderItems()).hasSize(2);
        assertThat(orders.getFirst().orderItems().get(0).productId()).isEqualTo(radioProduct.getId());
        assertThat(orders.getFirst().orderItems().get(1).productId()).isEqualTo(phoneProduct.getId());
    }
}
