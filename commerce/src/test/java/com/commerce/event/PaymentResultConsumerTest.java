package com.commerce.event;

import com.commerce.domain.Member;
import com.commerce.domain.Order;
import com.commerce.domain.OrderStatus;
import com.commerce.domain.Product;
import com.commerce.repository.MemberRepository;
import com.commerce.repository.OrderRepository;
import com.commerce.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class PaymentResultConsumerTest {

    @Autowired
    private PaymentResultConsumer paymentResultConsumer;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    private Order order;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(Member.builder()
                .username("member")
                .nickname("member")
                .build());

        Product product = productRepository.save(Product.builder()
                .name("radio")
                .description("radio description")
                .price(50000)
                .stock(100)
                .build());

        order = Order.builder().member(member).build();
        order.addOrderItem(product, 1);
        order = orderRepository.save(order);
    }

    @Test
    void 결제_성공_수신시_주문_상태_COMPLETED() throws JsonProcessingException {
        // given
        PaymentResultEvent event = new PaymentResultEvent(order.getId(), "결제 성공", OrderStatus.COMPLETED);
        String message = objectMapper.writeValueAsString(event);

        // when
        paymentResultConsumer.consume(message);

        // then
        Order updated = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void 결제_실패_수신시_주문_상태_CANCELLED() throws JsonProcessingException {
        // given
        PaymentResultEvent event = new PaymentResultEvent(order.getId(), "결제 실패", OrderStatus.CANCELLED);
        String message = objectMapper.writeValueAsString(event);

        // when
        paymentResultConsumer.consume(message);

        // then
        Order updated = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void 존재하지_않는_주문_결제_결과_수신시_예외_발생() throws JsonProcessingException {
        // given
        Long nonExistentOrderId = -1L;
        PaymentResultEvent event = new PaymentResultEvent(nonExistentOrderId, "결제 성공", OrderStatus.COMPLETED);
        String message = objectMapper.writeValueAsString(event);

        // when & then
        assertThatThrownBy(() -> paymentResultConsumer.consume(message))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 주문");
    }
}
